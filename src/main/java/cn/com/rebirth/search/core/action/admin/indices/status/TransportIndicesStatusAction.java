/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportIndicesStatusAction.java 2012-7-6 14:28:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.status;

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
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.gateway.IndexShardGatewayService;
import cn.com.rebirth.search.core.index.gateway.SnapshotStatus;
import cn.com.rebirth.search.core.index.service.InternalIndexService;
import cn.com.rebirth.search.core.index.shard.IndexShardState;
import cn.com.rebirth.search.core.index.shard.service.InternalIndexShard;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.indices.recovery.RecoveryStatus;
import cn.com.rebirth.search.core.indices.recovery.RecoveryTarget;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportIndicesStatusAction.
 *
 * @author l.xue.nong
 */
public class TransportIndicesStatusAction
		extends
		TransportBroadcastOperationAction<IndicesStatusRequest, IndicesStatusResponse, TransportIndicesStatusAction.IndexShardStatusRequest, ShardStatus> {

	/** The indices service. */
	private final IndicesService indicesService;

	/** The peer recovery target. */
	private final RecoveryTarget peerRecoveryTarget;

	/**
	 * Instantiates a new transport indices status action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 * @param peerRecoveryTarget the peer recovery target
	 */
	@Inject
	public TransportIndicesStatusAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			TransportService transportService, IndicesService indicesService, RecoveryTarget peerRecoveryTarget) {
		super(settings, threadPool, clusterService, transportService);
		this.peerRecoveryTarget = peerRecoveryTarget;
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
		return IndicesStatusAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newRequest()
	 */
	@Override
	protected IndicesStatusRequest newRequest() {
		return new IndicesStatusRequest();
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
	protected GroupShardsIterator shards(ClusterState state, IndicesStatusRequest request, String[] concreteIndices) {
		return state.routingTable().allAssignedShardsGrouped(concreteIndices, true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, IndicesStatusRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, IndicesStatusRequest countRequest,
			String[] concreteIndices) {
		return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, concreteIndices);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newResponse(cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected IndicesStatusResponse newResponse(IndicesStatusRequest request, AtomicReferenceArray shardsResponses,
			ClusterState clusterState) {
		int successfulShards = 0;
		int failedShards = 0;
		List<ShardOperationFailedException> shardFailures = null;
		final List<ShardStatus> shards = newArrayList();
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
				shards.add((ShardStatus) shardResponse);
				successfulShards++;
			}
		}
		return new IndicesStatusResponse(shards.toArray(new ShardStatus[shards.size()]), clusterState,
				shardsResponses.length(), successfulShards, failedShards, shardFailures);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest()
	 */
	@Override
	protected IndexShardStatusRequest newShardRequest() {
		return new IndexShardStatusRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected IndexShardStatusRequest newShardRequest(ShardRouting shard, IndicesStatusRequest request) {
		return new IndexShardStatusRequest(shard.index(), shard.id(), request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardResponse()
	 */
	@Override
	protected ShardStatus newShardResponse() {
		return new ShardStatus();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#shardOperation(cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest)
	 */
	@Override
	protected ShardStatus shardOperation(IndexShardStatusRequest request) throws RebirthException {
		InternalIndexService indexService = (InternalIndexService) indicesService.indexServiceSafe(request.index());
		InternalIndexShard indexShard = (InternalIndexShard) indexService.shardSafe(request.shardId());
		ShardStatus shardStatus = new ShardStatus(indexShard.routingEntry());
		shardStatus.state = indexShard.state();
		try {
			shardStatus.storeSize = indexShard.store().estimateSize();
		} catch (IOException e) {

		}
		if (indexShard.state() == IndexShardState.STARTED) {

			shardStatus.translogId = indexShard.translog().currentId();
			shardStatus.translogOperations = indexShard.translog().estimatedNumberOfOperations();
			Engine.Searcher searcher = indexShard.searcher();
			try {
				shardStatus.docs = new DocsStatus();
				shardStatus.docs.numDocs = searcher.reader().numDocs();
				shardStatus.docs.maxDoc = searcher.reader().maxDoc();
				shardStatus.docs.deletedDocs = searcher.reader().numDeletedDocs();
			} finally {
				searcher.release();
			}

			shardStatus.mergeStats = indexShard.mergeScheduler().stats();
			shardStatus.refreshStats = indexShard.refreshStats();
			shardStatus.flushStats = indexShard.flushStats();
		}

		if (request.recovery) {

			RecoveryStatus peerRecoveryStatus = indexShard.peerRecoveryStatus();
			if (peerRecoveryStatus == null) {
				peerRecoveryStatus = peerRecoveryTarget.peerRecoveryStatus(indexShard.shardId());
			}
			if (peerRecoveryStatus != null) {
				PeerRecoveryStatus.Stage stage;
				switch (peerRecoveryStatus.stage()) {
				case INIT:
					stage = PeerRecoveryStatus.Stage.INIT;
					break;
				case INDEX:
					stage = PeerRecoveryStatus.Stage.INDEX;
					break;
				case TRANSLOG:
					stage = PeerRecoveryStatus.Stage.TRANSLOG;
					break;
				case FINALIZE:
					stage = PeerRecoveryStatus.Stage.FINALIZE;
					break;
				case DONE:
					stage = PeerRecoveryStatus.Stage.DONE;
					break;
				default:
					stage = PeerRecoveryStatus.Stage.INIT;
				}
				shardStatus.peerRecoveryStatus = new PeerRecoveryStatus(stage, peerRecoveryStatus.startTime(),
						peerRecoveryStatus.time(), peerRecoveryStatus.phase1TotalSize(),
						peerRecoveryStatus.phase1ExistingTotalSize(), peerRecoveryStatus.currentFilesSize(),
						peerRecoveryStatus.currentTranslogOperations());
			}

			IndexShardGatewayService gatewayService = indexService.shardInjector(request.shardId()).getInstance(
					IndexShardGatewayService.class);
			cn.com.rebirth.search.core.index.gateway.RecoveryStatus gatewayRecoveryStatus = gatewayService
					.recoveryStatus();
			if (gatewayRecoveryStatus != null) {
				GatewayRecoveryStatus.Stage stage;
				switch (gatewayRecoveryStatus.stage()) {
				case INIT:
					stage = GatewayRecoveryStatus.Stage.INIT;
					break;
				case INDEX:
					stage = GatewayRecoveryStatus.Stage.INDEX;
					break;
				case TRANSLOG:
					stage = GatewayRecoveryStatus.Stage.TRANSLOG;
					break;
				case DONE:
					stage = GatewayRecoveryStatus.Stage.DONE;
					break;
				default:
					stage = GatewayRecoveryStatus.Stage.INIT;
				}
				shardStatus.gatewayRecoveryStatus = new GatewayRecoveryStatus(stage, gatewayRecoveryStatus.startTime(),
						gatewayRecoveryStatus.time(), gatewayRecoveryStatus.index().totalSize(), gatewayRecoveryStatus
								.index().reusedTotalSize(), gatewayRecoveryStatus.index().currentFilesSize(),
						gatewayRecoveryStatus.translog().currentTranslogOperations());
			}
		}

		if (request.snapshot) {
			IndexShardGatewayService gatewayService = indexService.shardInjector(request.shardId()).getInstance(
					IndexShardGatewayService.class);
			SnapshotStatus snapshotStatus = gatewayService.snapshotStatus();
			if (snapshotStatus != null) {
				GatewaySnapshotStatus.Stage stage;
				switch (snapshotStatus.stage()) {
				case DONE:
					stage = GatewaySnapshotStatus.Stage.DONE;
					break;
				case FAILURE:
					stage = GatewaySnapshotStatus.Stage.FAILURE;
					break;
				case TRANSLOG:
					stage = GatewaySnapshotStatus.Stage.TRANSLOG;
					break;
				case FINALIZE:
					stage = GatewaySnapshotStatus.Stage.FINALIZE;
					break;
				case INDEX:
					stage = GatewaySnapshotStatus.Stage.INDEX;
					break;
				default:
					stage = GatewaySnapshotStatus.Stage.NONE;
					break;
				}
				shardStatus.gatewaySnapshotStatus = new GatewaySnapshotStatus(stage, snapshotStatus.startTime(),
						snapshotStatus.time(), snapshotStatus.index().totalSize(), snapshotStatus.translog()
								.expectedNumberOfOperations());
			}
		}

		return shardStatus;
	}

	/**
	 * The Class IndexShardStatusRequest.
	 *
	 * @author l.xue.nong
	 */
	public static class IndexShardStatusRequest extends BroadcastShardOperationRequest {

		/** The recovery. */
		boolean recovery;

		/** The snapshot. */
		boolean snapshot;

		/**
		 * Instantiates a new index shard status request.
		 */
		IndexShardStatusRequest() {
		}

		/**
		 * Instantiates a new index shard status request.
		 *
		 * @param index the index
		 * @param shardId the shard id
		 * @param request the request
		 */
		IndexShardStatusRequest(String index, int shardId, IndicesStatusRequest request) {
			super(index, shardId);
			recovery = request.recovery();
			snapshot = request.snapshot();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			recovery = in.readBoolean();
			snapshot = in.readBoolean();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			out.writeBoolean(recovery);
			out.writeBoolean(snapshot);
		}
	}
}
