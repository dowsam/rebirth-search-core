/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportIndicesReplicationOperationAction.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.replication;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportIndicesReplicationOperationAction.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @param <IndexRequest> the generic type
 * @param <IndexResponse> the generic type
 * @param <ShardRequest> the generic type
 * @param <ShardReplicaRequest> the generic type
 * @param <ShardResponse> the generic type
 * @author l.xue.nong
 */
public abstract class TransportIndicesReplicationOperationAction<Request extends IndicesReplicationOperationRequest, Response extends ActionResponse, IndexRequest extends IndexReplicationOperationRequest, IndexResponse extends ActionResponse, ShardRequest extends ShardReplicationOperationRequest, ShardReplicaRequest extends ActionRequest, ShardResponse extends ActionResponse>
		extends TransportAction<Request, Response> {

	/** The cluster service. */
	protected final ClusterService clusterService;

	/** The index action. */
	protected final TransportIndexReplicationOperationAction<IndexRequest, IndexResponse, ShardRequest, ShardReplicaRequest, ShardResponse> indexAction;

	/** The transport action. */
	final String transportAction;

	/**
	 * Instantiates a new transport indices replication operation action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param indexAction the index action
	 */
	@Inject
	public TransportIndicesReplicationOperationAction(
			Settings settings,
			TransportService transportService,
			ClusterService clusterService,
			ThreadPool threadPool,
			TransportIndexReplicationOperationAction<IndexRequest, IndexResponse, ShardRequest, ShardReplicaRequest, ShardResponse> indexAction) {
		super(settings, threadPool);
		this.clusterService = clusterService;
		this.indexAction = indexAction;

		this.transportAction = transportAction();

		transportService.registerHandler(transportAction, new TransportHandler());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.TransportAction#doExecute(cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(final Request request, final ActionListener<Response> listener) {
		ClusterState clusterState = clusterService.state();
		ClusterBlockException blockException = checkGlobalBlock(clusterState, request);
		if (blockException != null) {
			throw blockException;
		}

		String[] concreteIndices = clusterState.metaData().concreteIndices(request.indices());
		blockException = checkRequestBlock(clusterState, request, concreteIndices);
		if (blockException != null) {
			throw blockException;
		}

		final AtomicInteger indexCounter = new AtomicInteger();
		final AtomicInteger completionCounter = new AtomicInteger(concreteIndices.length);
		final AtomicReferenceArray<Object> indexResponses = new AtomicReferenceArray<Object>(concreteIndices.length);

		Map<String, Set<String>> routingMap = clusterState.metaData().resolveSearchRouting(request.routing(),
				request.indices());

		for (final String index : concreteIndices) {
			Set<String> routing = null;
			if (routingMap != null) {
				routing = routingMap.get(index);
			}
			IndexRequest indexRequest = newIndexRequestInstance(request, index, routing);

			indexRequest.listenerThreaded(false);
			indexAction.execute(indexRequest, new ActionListener<IndexResponse>() {
				@Override
				public void onResponse(IndexResponse result) {
					indexResponses.set(indexCounter.getAndIncrement(), result);
					if (completionCounter.decrementAndGet() == 0) {
						listener.onResponse(newResponseInstance(request, indexResponses));
					}
				}

				@Override
				public void onFailure(Throwable e) {
					e.printStackTrace();
					int index = indexCounter.getAndIncrement();
					if (accumulateExceptions()) {
						indexResponses.set(index, e);
					}
					if (completionCounter.decrementAndGet() == 0) {
						listener.onResponse(newResponseInstance(request, indexResponses));
					}
				}
			});
		}
	}

	/**
	 * New request instance.
	 *
	 * @return the request
	 */
	protected abstract Request newRequestInstance();

	/**
	 * New response instance.
	 *
	 * @param request the request
	 * @param indexResponses the index responses
	 * @return the response
	 */
	protected abstract Response newResponseInstance(Request request, AtomicReferenceArray indexResponses);

	/**
	 * Transport action.
	 *
	 * @return the string
	 */
	protected abstract String transportAction();

	/**
	 * New index request instance.
	 *
	 * @param request the request
	 * @param index the index
	 * @param routing the routing
	 * @return the index request
	 */
	protected abstract IndexRequest newIndexRequestInstance(Request request, String index, Set<String> routing);

	/**
	 * Accumulate exceptions.
	 *
	 * @return true, if successful
	 */
	protected abstract boolean accumulateExceptions();

	/**
	 * Check global block.
	 *
	 * @param state the state
	 * @param request the request
	 * @return the cluster block exception
	 */
	protected abstract ClusterBlockException checkGlobalBlock(ClusterState state, Request request);

	/**
	 * Check request block.
	 *
	 * @param state the state
	 * @param request the request
	 * @param concreteIndices the concrete indices
	 * @return the cluster block exception
	 */
	protected abstract ClusterBlockException checkRequestBlock(ClusterState state, Request request,
			String[] concreteIndices);

	/**
	 * The Class TransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class TransportHandler extends BaseTransportRequestHandler<Request> {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public Request newInstance() {
			return newRequestInstance();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SAME;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(final Request request, final TransportChannel channel) throws Exception {

			request.listenerThreaded(false);
			execute(request, new ActionListener<Response>() {
				@Override
				public void onResponse(Response result) {
					try {
						channel.sendResponse(result);
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(e);
					} catch (Exception e1) {
						logger.warn("Failed to send error response for action [" + transportAction + "] and request ["
								+ request + "]", e1);
					}
				}
			});
		}
	}
}