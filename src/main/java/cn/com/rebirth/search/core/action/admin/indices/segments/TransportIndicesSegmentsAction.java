/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportIndicesSegmentsAction.java 2012-3-29 15:01:22 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.segments;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.DefaultShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest;
import cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.routing.GroupShardsIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.index.service.InternalIndexService;
import cn.com.rebirth.search.core.index.shard.service.InternalIndexShard;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;


/**
 * The Class TransportIndicesSegmentsAction.
 *
 * @author l.xue.nong
 */
public class TransportIndicesSegmentsAction
		extends
		TransportBroadcastOperationAction<IndicesSegmentsRequest, IndicesSegmentResponse, TransportIndicesSegmentsAction.IndexShardSegmentRequest, ShardSegments> {

	
	/** The indices service. */
	private final IndicesService indicesService;

	
	/**
	 * Instantiates a new transport indices segments action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 */
	@Inject
	public TransportIndicesSegmentsAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			TransportService transportService, IndicesService indicesService) {
		super(settings, threadPool, clusterService, transportService);
		this.indicesService = indicesService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.MANAGEMENT;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return IndicesSegmentsAction.NAME;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newRequest()
	 */
	@Override
	protected IndicesSegmentsRequest newRequest() {
		return new IndicesSegmentsRequest();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#ignoreNonActiveExceptions()
	 */
	@Override
	protected boolean ignoreNonActiveExceptions() {
		return true;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#shards(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected GroupShardsIterator shards(ClusterState clusterState, IndicesSegmentsRequest request,
			String[] concreteIndices) {
		return clusterState.routingTable().allActiveShardsGrouped(concreteIndices, true);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkGlobalBlock(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, IndicesSegmentsRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkRequestBlock(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, IndicesSegmentsRequest countRequest,
			String[] concreteIndices) {
		return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, concreteIndices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newResponse(cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray, cn.com.summall.search.core.cluster.ClusterState)
	 */
	@Override
	protected IndicesSegmentResponse newResponse(IndicesSegmentsRequest request, AtomicReferenceArray shardsResponses,
			ClusterState clusterState) {
		int successfulShards = 0;
		int failedShards = 0;
		List<ShardOperationFailedException> shardFailures = null;
		final List<ShardSegments> shards = newArrayList();
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
				shards.add((ShardSegments) shardResponse);
				successfulShards++;
			}
		}
		return new IndicesSegmentResponse(shards.toArray(new ShardSegments[shards.size()]), clusterState,
				shardsResponses.length(), successfulShards, failedShards, shardFailures);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest()
	 */
	@Override
	protected IndexShardSegmentRequest newShardRequest() {
		return new IndexShardSegmentRequest();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest(cn.com.summall.search.core.cluster.routing.ShardRouting, cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected IndexShardSegmentRequest newShardRequest(ShardRouting shard, IndicesSegmentsRequest request) {
		return new IndexShardSegmentRequest(shard.index(), shard.id(), request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardResponse()
	 */
	@Override
	protected ShardSegments newShardResponse() {
		return new ShardSegments();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.TransportBroadcastOperationAction#shardOperation(cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationRequest)
	 */
	@Override
	protected ShardSegments shardOperation(IndexShardSegmentRequest request) throws RestartException {
		InternalIndexService indexService = (InternalIndexService) indicesService.indexServiceSafe(request.index());
		InternalIndexShard indexShard = (InternalIndexShard) indexService.shardSafe(request.shardId());
		return new ShardSegments(indexShard.routingEntry(), indexShard.engine().segments());
	}

	
	/**
	 * The Class IndexShardSegmentRequest.
	 *
	 * @author l.xue.nong
	 */
	public static class IndexShardSegmentRequest extends BroadcastShardOperationRequest {

		
		/**
		 * Instantiates a new index shard segment request.
		 */
		IndexShardSegmentRequest() {
		}

		
		/**
		 * Instantiates a new index shard segment request.
		 *
		 * @param index the index
		 * @param shardId the shard id
		 * @param request the request
		 */
		IndexShardSegmentRequest(String index, int shardId, IndicesSegmentsRequest request) {
			super(index, shardId);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
		}
	}
}
