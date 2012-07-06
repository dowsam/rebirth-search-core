/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MetaDataStateIndexService.java 2012-3-29 15:02:48 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.metadata;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ProcessedClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.block.ClusterBlock;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.block.ClusterBlocks;
import cn.com.rebirth.search.core.cluster.routing.IndexRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.cluster.routing.allocation.AllocationService;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.indices.IndexMissingException;
import cn.com.rebirth.search.core.rest.RestStatus;


/**
 * The Class MetaDataStateIndexService.
 *
 * @author l.xue.nong
 */
public class MetaDataStateIndexService extends AbstractComponent {

	
	/** The Constant INDEX_CLOSED_BLOCK. */
	public static final ClusterBlock INDEX_CLOSED_BLOCK = new ClusterBlock(4, "index closed", false, false,
			RestStatus.FORBIDDEN, ClusterBlockLevel.READ_WRITE);

	
	/** The cluster service. */
	private final ClusterService clusterService;

	
	/** The allocation service. */
	private final AllocationService allocationService;

	
	/**
	 * Instantiates a new meta data state index service.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 * @param allocationService the allocation service
	 */
	@Inject
	public MetaDataStateIndexService(Settings settings, ClusterService clusterService,
			AllocationService allocationService) {
		super(settings);
		this.clusterService = clusterService;
		this.allocationService = allocationService;
	}

	
	/**
	 * Close index.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	public void closeIndex(final Request request, final Listener listener) {
		clusterService.submitStateUpdateTask("close-index [" + request.index + "]",
				new ProcessedClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {

						IndexMetaData indexMetaData = currentState.metaData().index(request.index);
						if (indexMetaData == null) {
							listener.onFailure(new IndexMissingException(new Index(request.index)));
							return currentState;
						}

						if (indexMetaData.state() == IndexMetaData.State.CLOSE) {
							listener.onResponse(new Response(true));
							return currentState;
						}

						logger.info("[{}] closing index", request.index);

						MetaData.Builder mdBuilder = MetaData
								.builder()
								.metaData(currentState.metaData())
								.put(IndexMetaData
										.newIndexMetaDataBuilder(currentState.metaData().index(request.index)).state(
												IndexMetaData.State.CLOSE));

						ClusterBlocks.Builder blocks = ClusterBlocks.builder().blocks(currentState.blocks())
								.addIndexBlock(request.index, INDEX_CLOSED_BLOCK);

						ClusterState updatedState = ClusterState.builder().state(currentState).metaData(mdBuilder)
								.blocks(blocks).build();

						RoutingTable.Builder rtBuilder = RoutingTable.builder()
								.routingTable(currentState.routingTable()).remove(request.index);

						RoutingAllocation.Result routingResult = allocationService.reroute(ClusterState.newClusterStateBuilder()
								.state(updatedState).routingTable(rtBuilder).build());

						return ClusterState.builder().state(updatedState).routingResult(routingResult).build();
					}

					@Override
					public void clusterStateProcessed(ClusterState clusterState) {
						listener.onResponse(new Response(true));
					}
				});
	}

	
	/**
	 * Open index.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	public void openIndex(final Request request, final Listener listener) {
		clusterService.submitStateUpdateTask("open-index [" + request.index + "]",
				new ProcessedClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {

						IndexMetaData indexMetaData = currentState.metaData().index(request.index);
						if (indexMetaData == null) {
							listener.onFailure(new IndexMissingException(new Index(request.index)));
							return currentState;
						}

						if (indexMetaData.state() == IndexMetaData.State.OPEN) {
							listener.onResponse(new Response(true));
							return currentState;
						}

						logger.info("[{}] opening index", request.index);

						MetaData.Builder mdBuilder = MetaData
								.builder()
								.metaData(currentState.metaData())
								.put(IndexMetaData
										.newIndexMetaDataBuilder(currentState.metaData().index(request.index)).state(
												IndexMetaData.State.OPEN));

						ClusterBlocks.Builder blocks = ClusterBlocks.builder().blocks(currentState.blocks())
								.removeIndexBlock(request.index, INDEX_CLOSED_BLOCK);

						ClusterState updatedState = ClusterState.builder().state(currentState).metaData(mdBuilder)
								.blocks(blocks).build();

						RoutingTable.Builder rtBuilder = RoutingTable.builder().routingTable(
								updatedState.routingTable());
						IndexRoutingTable.Builder indexRoutingBuilder = new IndexRoutingTable.Builder(request.index)
								.initializeEmpty(updatedState.metaData().index(request.index), false);
						rtBuilder.add(indexRoutingBuilder);

						RoutingAllocation.Result routingResult = allocationService.reroute(ClusterState.newClusterStateBuilder()
								.state(updatedState).routingTable(rtBuilder).build());

						return ClusterState.builder().state(updatedState).routingResult(routingResult).build();
					}

					@Override
					public void clusterStateProcessed(ClusterState clusterState) {
						listener.onResponse(new Response(true));
					}
				});
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
