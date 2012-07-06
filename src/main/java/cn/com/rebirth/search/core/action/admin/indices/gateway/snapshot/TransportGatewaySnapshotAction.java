/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportGatewaySnapshotAction.java 2012-3-29 15:01:08 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
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
import cn.com.rebirth.search.core.index.gateway.IndexShardGatewayService;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.Lists;


/**
 * The Class TransportGatewaySnapshotAction.
 *
 * @author l.xue.nong
 */
public class TransportGatewaySnapshotAction
		extends
		TransportBroadcastOperationAction<GatewaySnapshotRequest, GatewaySnapshotResponse, ShardGatewaySnapshotRequest, ShardGatewaySnapshotResponse> {

	
	/** The indices service. */
	private final IndicesService indicesService;

	
	/**
	 * Instantiates a new transport gateway snapshot action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 */
	@Inject
	public TransportGatewaySnapshotAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			TransportService transportService, IndicesService indicesService) {
		super(settings, threadPool, clusterService, transportService);
		this.indicesService = indicesService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.SNAPSHOT;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return GatewaySnapshotAction.NAME;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newRequest()
	 */
	@Override
	protected GatewaySnapshotRequest newRequest() {
		return new GatewaySnapshotRequest();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#ignoreNonActiveExceptions()
	 */
	@Override
	protected boolean ignoreNonActiveExceptions() {
		return true;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newResponse(cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray, cn.com.summall.search.core.cluster.ClusterState)
	 */
	@Override
	protected GatewaySnapshotResponse newResponse(GatewaySnapshotRequest request, AtomicReferenceArray shardsResponses,
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
					shardFailures = Lists.newArrayList();
				}
				shardFailures.add(new DefaultShardOperationFailedException(
						(BroadcastShardOperationFailedException) shardResponse));
			} else {
				successfulShards++;
			}
		}
		return new GatewaySnapshotResponse(shardsResponses.length(), successfulShards, failedShards, shardFailures);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest()
	 */
	@Override
	protected ShardGatewaySnapshotRequest newShardRequest() {
		return new ShardGatewaySnapshotRequest();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest(cn.com.summall.search.core.cluster.routing.ShardRouting, cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected ShardGatewaySnapshotRequest newShardRequest(ShardRouting shard, GatewaySnapshotRequest request) {
		return new ShardGatewaySnapshotRequest(shard.index(), shard.id());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardResponse()
	 */
	@Override
	protected ShardGatewaySnapshotResponse newShardResponse() {
		return new ShardGatewaySnapshotResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#shardOperation(cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationRequest)
	 */
	@Override
	protected ShardGatewaySnapshotResponse shardOperation(ShardGatewaySnapshotRequest request)
			throws RestartException {
		IndexShardGatewayService shardGatewayService = indicesService.indexServiceSafe(request.index())
				.shardInjectorSafe(request.shardId()).getInstance(IndexShardGatewayService.class);
		shardGatewayService.snapshot("api");
		return new ShardGatewaySnapshotResponse(request.index(), request.shardId());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#shards(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected GroupShardsIterator shards(ClusterState clusterState, GatewaySnapshotRequest request,
			String[] concreteIndices) {
		return clusterState.routingTable().activePrimaryShardsGrouped(concreteIndices, true);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkGlobalBlock(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, GatewaySnapshotRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkRequestBlock(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, GatewaySnapshotRequest request,
			String[] concreteIndices) {
		return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, concreteIndices);
	}

}
