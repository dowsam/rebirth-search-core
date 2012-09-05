/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportOptimizeAction.java 2012-7-6 14:29:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.optimize;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

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
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportOptimizeAction.
 *
 * @author l.xue.nong
 */
public class TransportOptimizeAction
		extends
		TransportBroadcastOperationAction<OptimizeRequest, OptimizeResponse, ShardOptimizeRequest, ShardOptimizeResponse> {

	/** The indices service. */
	private final IndicesService indicesService;

	/** The optimize mutex. */
	private final Object optimizeMutex = new Object();

	/**
	 * Instantiates a new transport optimize action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 */
	@Inject
	public TransportOptimizeAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			TransportService transportService, IndicesService indicesService) {
		super(settings, threadPool, clusterService, transportService);
		this.indicesService = indicesService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.MERGE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return OptimizeAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newRequest()
	 */
	@Override
	protected OptimizeRequest newRequest() {
		return new OptimizeRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#ignoreNonActiveExceptions()
	 */
	@Override
	protected boolean ignoreNonActiveExceptions() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newResponse(cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected OptimizeResponse newResponse(OptimizeRequest request, AtomicReferenceArray shardsResponses,
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
		return new OptimizeResponse(shardsResponses.length(), successfulShards, failedShards, shardFailures);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest()
	 */
	@Override
	protected ShardOptimizeRequest newShardRequest() {
		return new ShardOptimizeRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected ShardOptimizeRequest newShardRequest(ShardRouting shard, OptimizeRequest request) {
		return new ShardOptimizeRequest(shard.index(), shard.id(), request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardResponse()
	 */
	@Override
	protected ShardOptimizeResponse newShardResponse() {
		return new ShardOptimizeResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#shardOperation(cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest)
	 */
	@Override
	protected ShardOptimizeResponse shardOperation(ShardOptimizeRequest request) throws RebirthException {
		synchronized (optimizeMutex) {
			IndexShard indexShard = indicesService.indexServiceSafe(request.index()).shardSafe(request.shardId());
			indexShard.optimize(new Engine.Optimize().waitForMerge(request.waitForMerge())
					.maxNumSegments(request.maxNumSegments()).onlyExpungeDeletes(request.onlyExpungeDeletes())
					.flush(request.flush()).refresh(request.refresh()));
			return new ShardOptimizeResponse(request.index(), request.shardId());
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#shards(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected GroupShardsIterator shards(ClusterState clusterState, OptimizeRequest request, String[] concreteIndices) {
		return clusterState.routingTable().allActiveShardsGrouped(concreteIndices, true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, OptimizeRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, OptimizeRequest request,
			String[] concreteIndices) {
		return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, concreteIndices);
	}
}