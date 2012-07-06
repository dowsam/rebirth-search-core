/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportFlushAction.java 2012-3-29 15:02:53 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.flush;

import static com.google.common.collect.Lists.newArrayList;

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
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;


/**
 * The Class TransportFlushAction.
 *
 * @author l.xue.nong
 */
public class TransportFlushAction extends
		TransportBroadcastOperationAction<FlushRequest, FlushResponse, ShardFlushRequest, ShardFlushResponse> {

	
	/** The indices service. */
	private final IndicesService indicesService;

	
	/**
	 * Instantiates a new transport flush action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 */
	@Inject
	public TransportFlushAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			TransportService transportService, IndicesService indicesService) {
		super(settings, threadPool, clusterService, transportService);
		this.indicesService = indicesService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.FLUSH;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return FlushAction.NAME;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newRequest()
	 */
	@Override
	protected FlushRequest newRequest() {
		return new FlushRequest();
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
	protected FlushResponse newResponse(FlushRequest request, AtomicReferenceArray shardsResponses,
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
		return new FlushResponse(shardsResponses.length(), successfulShards, failedShards, shardFailures);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest()
	 */
	@Override
	protected ShardFlushRequest newShardRequest() {
		return new ShardFlushRequest();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest(cn.com.summall.search.core.cluster.routing.ShardRouting, cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected ShardFlushRequest newShardRequest(ShardRouting shard, FlushRequest request) {
		return new ShardFlushRequest(shard.index(), shard.id(), request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardResponse()
	 */
	@Override
	protected ShardFlushResponse newShardResponse() {
		return new ShardFlushResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#shardOperation(cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationRequest)
	 */
	@Override
	protected ShardFlushResponse shardOperation(ShardFlushRequest request) throws RestartException {
		IndexShard indexShard = indicesService.indexServiceSafe(request.index()).shardSafe(request.shardId());
		indexShard.flush(new Engine.Flush().refresh(request.refresh()).full(request.full()).force(request.force()));
		return new ShardFlushResponse(request.index(), request.shardId());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#shards(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected GroupShardsIterator shards(ClusterState clusterState, FlushRequest request, String[] concreteIndices) {
		return clusterState.routingTable().allActiveShardsGrouped(concreteIndices, true);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkGlobalBlock(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, FlushRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkRequestBlock(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, FlushRequest countRequest,
			String[] concreteIndices) {
		return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, concreteIndices);
	}
}