/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MetaDataDeleteIndexService.java 2012-7-6 14:29:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.metadata;

import static cn.com.rebirth.search.core.cluster.ClusterState.newClusterStateBuilder;
import static cn.com.rebirth.search.core.cluster.metadata.MetaData.newMetaDataBuilder;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.action.index.NodeIndexDeletedAction;
import cn.com.rebirth.search.core.cluster.block.ClusterBlocks;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.cluster.routing.allocation.AllocationService;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.indices.IndexMissingException;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

/**
 * The Class MetaDataDeleteIndexService.
 *
 * @author l.xue.nong
 */
public class MetaDataDeleteIndexService extends AbstractComponent {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The allocation service. */
	private final AllocationService allocationService;

	/** The node index deleted action. */
	private final NodeIndexDeletedAction nodeIndexDeletedAction;

	/** The meta data service. */
	private final MetaDataService metaDataService;

	/**
	 * Instantiates a new meta data delete index service.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param allocationService the allocation service
	 * @param nodeIndexDeletedAction the node index deleted action
	 * @param metaDataService the meta data service
	 */
	@Inject
	public MetaDataDeleteIndexService(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			AllocationService allocationService, NodeIndexDeletedAction nodeIndexDeletedAction,
			MetaDataService metaDataService) {
		super(settings);
		this.threadPool = threadPool;
		this.clusterService = clusterService;
		this.allocationService = allocationService;
		this.nodeIndexDeletedAction = nodeIndexDeletedAction;
		this.metaDataService = metaDataService;
	}

	/**
	 * Delete index.
	 *
	 * @param request the request
	 * @param userListener the user listener
	 */
	public void deleteIndex(final Request request, final Listener userListener) {

		MetaDataService.MdLock mdLock = metaDataService.indexMetaDataLock(request.index);
		try {
			mdLock.lock();
		} catch (InterruptedException e) {
			userListener.onFailure(e);
			return;
		}

		final DeleteIndexListener listener = new DeleteIndexListener(mdLock, request, userListener);
		clusterService.submitStateUpdateTask("delete-index [" + request.index + "]", new ClusterStateUpdateTask() {
			@Override
			public ClusterState execute(ClusterState currentState) {
				try {
					if (!currentState.metaData().hasConcreteIndex(request.index)) {
						listener.onFailure(new IndexMissingException(new Index(request.index)));
						return currentState;
					}

					logger.info("[{}] deleting index", request.index);

					RoutingTable.Builder routingTableBuilder = RoutingTable.builder().routingTable(
							currentState.routingTable());
					routingTableBuilder.remove(request.index);

					MetaData newMetaData = newMetaDataBuilder().metaData(currentState.metaData()).remove(request.index)
							.build();

					RoutingAllocation.Result routingResult = allocationService.reroute(newClusterStateBuilder()
							.state(currentState).routingTable(routingTableBuilder).metaData(newMetaData).build());

					ClusterBlocks blocks = ClusterBlocks.builder().blocks(currentState.blocks())
							.removeIndexBlocks(request.index).build();

					final AtomicInteger counter = new AtomicInteger(currentState.nodes().size());

					final NodeIndexDeletedAction.Listener nodeIndexDeleteListener = new NodeIndexDeletedAction.Listener() {
						@Override
						public void onNodeIndexDeleted(String index, String nodeId) {
							if (index.equals(request.index)) {
								if (counter.decrementAndGet() == 0) {
									listener.onResponse(new Response(true));
									nodeIndexDeletedAction.remove(this);
								}
							}
						}
					};
					nodeIndexDeletedAction.add(nodeIndexDeleteListener);

					listener.future = threadPool.schedule(request.timeout, ThreadPool.Names.SAME, new Runnable() {
						@Override
						public void run() {
							listener.onResponse(new Response(false));
							nodeIndexDeletedAction.remove(nodeIndexDeleteListener);
						}
					});

					return newClusterStateBuilder().state(currentState).routingResult(routingResult)
							.metaData(newMetaData).blocks(blocks).build();
				} catch (Exception e) {
					listener.onFailure(e);
					return currentState;
				}
			}
		});
	}

	/**
	 * The listener interface for receiving deleteIndex events.
	 * The class that is interested in processing a deleteIndex
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addDeleteIndexListener<code> method. When
	 * the deleteIndex event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see DeleteIndexEvent
	 */
	class DeleteIndexListener implements Listener {

		/** The notified. */
		private final AtomicBoolean notified = new AtomicBoolean();

		/** The md lock. */
		private final MetaDataService.MdLock mdLock;

		/** The request. */
		private final Request request;

		/** The listener. */
		private final Listener listener;

		/** The future. */
		volatile ScheduledFuture future;

		/**
		 * Instantiates a new delete index listener.
		 *
		 * @param mdLock the md lock
		 * @param request the request
		 * @param listener the listener
		 */
		private DeleteIndexListener(MetaDataService.MdLock mdLock, Request request, Listener listener) {
			this.mdLock = mdLock;
			this.request = request;
			this.listener = listener;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.cluster.metadata.MetaDataDeleteIndexService.Listener#onResponse(cn.com.rebirth.search.core.cluster.metadata.MetaDataDeleteIndexService.Response)
		 */
		@Override
		public void onResponse(final Response response) {
			if (notified.compareAndSet(false, true)) {
				mdLock.unlock();
				if (future != null) {
					future.cancel(false);
				}
				listener.onResponse(response);
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.cluster.metadata.MetaDataDeleteIndexService.Listener#onFailure(java.lang.Throwable)
		 */
		@Override
		public void onFailure(Throwable t) {
			if (notified.compareAndSet(false, true)) {
				mdLock.unlock();
				if (future != null) {
					future.cancel(false);
				}
				listener.onFailure(t);
			}
		}
	}

	/**
	 * The Interface Listener.
	 *
	 * @author l.xue.nong
	 */
	public static interface Listener {

		/**
		 * On response.
		 *
		 * @param response the response
		 */
		void onResponse(Response response);

		/**
		 * On failure.
		 *
		 * @param t the t
		 */
		void onFailure(Throwable t);
	}

	/**
	 * The Class Request.
	 *
	 * @author l.xue.nong
	 */
	public static class Request {

		/** The index. */
		final String index;

		/** The timeout. */
		TimeValue timeout = TimeValue.timeValueSeconds(10);

		/**
		 * Instantiates a new request.
		 *
		 * @param index the index
		 */
		public Request(String index) {
			this.index = index;
		}

		/**
		 * Timeout.
		 *
		 * @param timeout the timeout
		 * @return the request
		 */
		public Request timeout(TimeValue timeout) {
			this.timeout = timeout;
			return this;
		}
	}

	/**
	 * The Class Response.
	 *
	 * @author l.xue.nong
	 */
	public static class Response {

		/** The acknowledged. */
		private final boolean acknowledged;

		/**
		 * Instantiates a new response.
		 *
		 * @param acknowledged the acknowledged
		 */
		public Response(boolean acknowledged) {
			this.acknowledged = acknowledged;
		}

		/**
		 * Acknowledged.
		 *
		 * @return true, if successful
		 */
		public boolean acknowledged() {
			return acknowledged;
		}
	}
}
