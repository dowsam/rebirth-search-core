/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportIndexReplicationOperationAction.java 2012-7-6 14:29:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.replication;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.routing.GroupShardsIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportIndexReplicationOperationAction.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @param <ShardRequest> the generic type
 * @param <ShardReplicaRequest> the generic type
 * @param <ShardResponse> the generic type
 * @author l.xue.nong
 */
public abstract class TransportIndexReplicationOperationAction<Request extends IndexReplicationOperationRequest, Response extends ActionResponse, ShardRequest extends ShardReplicationOperationRequest, ShardReplicaRequest extends ActionRequest, ShardResponse extends ActionResponse>
		extends TransportAction<Request, Response> {

	/** The cluster service. */
	protected final ClusterService clusterService;

	/** The shard action. */
	protected final TransportShardReplicationOperationAction<ShardRequest, ShardReplicaRequest, ShardResponse> shardAction;

	/**
	 * Instantiates a new transport index replication operation action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param shardAction the shard action
	 */
	@Inject
	public TransportIndexReplicationOperationAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool,
			TransportShardReplicationOperationAction<ShardRequest, ShardReplicaRequest, ShardResponse> shardAction) {
		super(settings, threadPool);
		this.clusterService = clusterService;
		this.shardAction = shardAction;

		transportService.registerHandler(transportAction(), new TransportHandler());
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

		request.index(clusterState.metaData().concreteIndex(request.index()));
		blockException = checkRequestBlock(clusterState, request);
		if (blockException != null) {
			throw blockException;
		}

		GroupShardsIterator groups;
		try {
			groups = shards(request);
		} catch (Exception e) {
			listener.onFailure(e);
			return;
		}
		final AtomicInteger indexCounter = new AtomicInteger();
		final AtomicInteger completionCounter = new AtomicInteger(groups.size());
		final AtomicReferenceArray<Object> shardsResponses = new AtomicReferenceArray<Object>(groups.size());

		for (final ShardIterator shardIt : groups) {
			ShardRequest shardRequest = newShardRequestInstance(request, shardIt.shardId().id());

			shardRequest.beforeLocalFork();
			shardRequest.operationThreaded(true);

			shardRequest.listenerThreaded(false);
			shardAction.execute(shardRequest, new ActionListener<ShardResponse>() {
				@Override
				public void onResponse(ShardResponse result) {
					shardsResponses.set(indexCounter.getAndIncrement(), result);
					if (completionCounter.decrementAndGet() == 0) {
						listener.onResponse(newResponseInstance(request, shardsResponses));
					}
				}

				@Override
				public void onFailure(Throwable e) {
					int index = indexCounter.getAndIncrement();
					if (accumulateExceptions()) {
						shardsResponses.set(index, e);
					}
					if (completionCounter.decrementAndGet() == 0) {
						listener.onResponse(newResponseInstance(request, shardsResponses));
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
	 * @param shardsResponses the shards responses
	 * @return the response
	 */
	protected abstract Response newResponseInstance(Request request, AtomicReferenceArray shardsResponses);

	/**
	 * Transport action.
	 *
	 * @return the string
	 */
	protected abstract String transportAction();

	/**
	 * Shards.
	 *
	 * @param request the request
	 * @return the group shards iterator
	 * @throws RebirthException the rebirth exception
	 */
	protected abstract GroupShardsIterator shards(Request request) throws RebirthException;

	/**
	 * New shard request instance.
	 *
	 * @param request the request
	 * @param shardId the shard id
	 * @return the shard request
	 */
	protected abstract ShardRequest newShardRequestInstance(Request request, int shardId);

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
	 * @return the cluster block exception
	 */
	protected abstract ClusterBlockException checkRequestBlock(ClusterState state, Request request);

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
						logger.warn("Failed to send error response for action [" + transportAction()
								+ "] and request [" + request + "]", e1);
					}
				}
			});
		}
	}
}