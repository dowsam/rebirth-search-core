/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportIndicesStatsAction.java 2012-7-6 14:29:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.stats;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
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
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.Lists;

/**
 * The Class TransportIndicesStatsAction.
 *
 * @author l.xue.nong
 */
public class TransportIndicesStatsAction
		extends
		TransportBroadcastOperationAction<IndicesStatsRequest, IndicesStats, TransportIndicesStatsAction.IndexShardStatsRequest, ShardStats> {

	/** The indices service. */
	private final IndicesService indicesService;

	/**
	 * Instantiates a new transport indices stats action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 */
	@Inject
	public TransportIndicesStatsAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			TransportService transportService, IndicesService indicesService) {
		super(settings, threadPool, clusterService, transportService);
		this.indicesService = indicesService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.MANAGEMENT;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return IndicesStatsAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newRequest()
	 */
	@Override
	protected IndicesStatsRequest newRequest() {
		return new IndicesStatsRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#ignoreNonActiveExceptions()
	 */
	@Override
	protected boolean ignoreNonActiveExceptions() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#shards(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected GroupShardsIterator shards(ClusterState clusterState, IndicesStatsRequest request,
			String[] concreteIndices) {
		return clusterState.routingTable().allAssignedShardsGrouped(concreteIndices, true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, IndicesStatsRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, IndicesStatsRequest request,
			String[] concreteIndices) {
		return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, concreteIndices);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newResponse(cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected IndicesStats newResponse(IndicesStatsRequest request, AtomicReferenceArray shardsResponses,
			ClusterState clusterState) {
		int successfulShards = 0;
		int failedShards = 0;
		List<ShardOperationFailedException> shardFailures = null;
		final List<ShardStats> shards = Lists.newArrayList();
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
				shards.add((ShardStats) shardResponse);
				successfulShards++;
			}
		}
		return new IndicesStats(shards.toArray(new ShardStats[shards.size()]), clusterState, shardsResponses.length(),
				successfulShards, failedShards, shardFailures);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest()
	 */
	@Override
	protected IndexShardStatsRequest newShardRequest() {
		return new IndexShardStatsRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected IndexShardStatsRequest newShardRequest(ShardRouting shard, IndicesStatsRequest request) {
		return new IndexShardStatsRequest(shard.index(), shard.id(), request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardResponse()
	 */
	@Override
	protected ShardStats newShardResponse() {
		return new ShardStats();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#shardOperation(cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest)
	 */
	@Override
	protected ShardStats shardOperation(IndexShardStatsRequest request) throws RebirthException {
		InternalIndexService indexService = (InternalIndexService) indicesService.indexServiceSafe(request.index());
		InternalIndexShard indexShard = (InternalIndexShard) indexService.shardSafe(request.shardId());
		ShardStats stats = new ShardStats(indexShard.routingEntry());

		if (request.request.docs()) {
			stats.stats.docs = indexShard.docStats();
		}
		if (request.request.store()) {
			stats.stats.store = indexShard.storeStats();
		}
		if (request.request.indexing()) {
			stats.stats.indexing = indexShard.indexingStats(request.request.types());
		}
		if (request.request.get()) {
			stats.stats.get = indexShard.getStats();
		}
		if (request.request.search()) {
			stats.stats().search = indexShard.searchStats(request.request.groups());
		}
		if (request.request.merge()) {
			stats.stats.merge = indexShard.mergeStats();
		}
		if (request.request.refresh()) {
			stats.stats.refresh = indexShard.refreshStats();
		}
		if (request.request.flush()) {
			stats.stats.flush = indexShard.flushStats();
		}

		return stats;
	}

	/**
	 * The Class IndexShardStatsRequest.
	 *
	 * @author l.xue.nong
	 */
	public static class IndexShardStatsRequest extends BroadcastShardOperationRequest {

		/** The request. */
		IndicesStatsRequest request;

		/**
		 * Instantiates a new index shard stats request.
		 */
		IndexShardStatsRequest() {
		}

		/**
		 * Instantiates a new index shard stats request.
		 *
		 * @param index the index
		 * @param shardId the shard id
		 * @param request the request
		 */
		IndexShardStatsRequest(String index, int shardId, IndicesStatsRequest request) {
			super(index, shardId);
			this.request = request;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			request = new IndicesStatsRequest();
			request.readFrom(in);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			request.writeTo(out);
		}
	}
}
