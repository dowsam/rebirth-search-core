/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportRefreshAction.java 2012-7-6 14:30:41 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.refresh;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.DefaultShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.routing.GroupShardsIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.index.IndexShardMissingException;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.shard.IllegalIndexShardStateException;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.indices.IndexMissingException;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportRefreshAction.
 *
 * @author l.xue.nong
 */
public class TransportRefreshAction extends
		TransportBroadcastOperationAction<RefreshRequest, RefreshResponse, ShardRefreshRequest, ShardRefreshResponse> {

	/** The indices service. */
	private final IndicesService indicesService;

	/**
	 * Instantiates a new transport refresh action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 */
	@Inject
	public TransportRefreshAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			TransportService transportService, IndicesService indicesService) {
		super(settings, threadPool, clusterService, transportService);
		this.indicesService = indicesService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.REFRESH;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return RefreshAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newRequest()
	 */
	@Override
	protected RefreshRequest newRequest() {
		return new RefreshRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#ignoreNonActiveExceptions()
	 */
	@Override
	protected boolean ignoreNonActiveExceptions() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#ignoreException(java.lang.Throwable)
	 */
	@Override
	protected boolean ignoreException(Throwable t) {
		Throwable actual = ExceptionsHelper.unwrapCause(t);
		if (actual instanceof IllegalIndexShardStateException) {
			return true;
		}
		if (actual instanceof IndexMissingException) {
			return true;
		}
		if (actual instanceof IndexShardMissingException) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newResponse(cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected RefreshResponse newResponse(RefreshRequest request, AtomicReferenceArray shardsResponses,
			ClusterState clusterState) {
		int successfulShards = 0;
		int failedShards = 0;
		List<ShardOperationFailedException> shardFailures = null;
		for (int i = 0; i < shardsResponses.length(); i++) {
			Object shardResponse = shardsResponses.get(i);
			if (shardResponse == null) {

			} else if (shardResponse instanceof BroadcastShardOperationFailedException) {
				failedShards++;
				if (shardFailures == null) {
					shardFailures = newArrayList();
				}
				shardFailures.add(new DefaultShardOperationFailedException(
						(BroadcastShardOperationFailedException) shardResponse));
			} else {
				successfulShards++;
			}
		}
		return new RefreshResponse(shardsResponses.length(), successfulShards, failedShards, shardFailures);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest()
	 */
	@Override
	protected ShardRefreshRequest newShardRequest() {
		return new ShardRefreshRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected ShardRefreshRequest newShardRequest(ShardRouting shard, RefreshRequest request) {
		return new ShardRefreshRequest(shard.index(), shard.id(), request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardResponse()
	 */
	@Override
	protected ShardRefreshResponse newShardResponse() {
		return new ShardRefreshResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#shardOperation(cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest)
	 */
	@Override
	protected ShardRefreshResponse shardOperation(ShardRefreshRequest request) throws RebirthException {
		IndexShard indexShard = indicesService.indexServiceSafe(request.index()).shardSafe(request.shardId());
		indexShard.refresh(new Engine.Refresh(request.waitForOperations()));
		return new ShardRefreshResponse(request.index(), request.shardId());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#shards(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected GroupShardsIterator shards(ClusterState clusterState, RefreshRequest request, String[] concreteIndices) {
		return clusterState.routingTable().allAssignedShardsGrouped(concreteIndices, true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, RefreshRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, RefreshRequest countRequest,
			String[] concreteIndices) {
		return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, concreteIndices);
	}
}
