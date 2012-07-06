/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportShardDeleteAction.java 2012-7-6 14:29:18 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.delete.index;

import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.action.shard.ShardStateAction;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.routing.GroupShardsIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportShardDeleteAction.
 *
 * @author l.xue.nong
 */
public class TransportShardDeleteAction extends
		TransportShardReplicationOperationAction<ShardDeleteRequest, ShardDeleteRequest, ShardDeleteResponse> {

	/**
	 * Instantiates a new transport shard delete action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param indicesService the indices service
	 * @param threadPool the thread pool
	 * @param shardStateAction the shard state action
	 */
	@Inject
	public TransportShardDeleteAction(Settings settings, TransportService transportService,
			ClusterService clusterService, IndicesService indicesService, ThreadPool threadPool,
			ShardStateAction shardStateAction) {
		super(settings, transportService, clusterService, indicesService, threadPool, shardStateAction);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#checkWriteConsistency()
	 */
	@Override
	protected boolean checkWriteConsistency() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newRequestInstance()
	 */
	@Override
	protected ShardDeleteRequest newRequestInstance() {
		return new ShardDeleteRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newReplicaRequestInstance()
	 */
	@Override
	protected ShardDeleteRequest newReplicaRequestInstance() {
		return new ShardDeleteRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newResponseInstance()
	 */
	@Override
	protected ShardDeleteResponse newResponseInstance() {
		return new ShardDeleteResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return "indices/index/b_shard/delete";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.INDEX;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, ShardDeleteRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.WRITE);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, ShardDeleteRequest request) {
		return state.blocks().indexBlockedException(ClusterBlockLevel.WRITE, request.index());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shardOperationOnPrimary(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction.PrimaryOperationRequest)
	 */
	@Override
	protected PrimaryResponse<ShardDeleteResponse, ShardDeleteRequest> shardOperationOnPrimary(
			ClusterState clusterState, PrimaryOperationRequest shardRequest) {
		ShardDeleteRequest request = shardRequest.request;
		IndexShard indexShard = indicesService.indexServiceSafe(shardRequest.request.index()).shardSafe(
				shardRequest.shardId);
		Engine.Delete delete = indexShard.prepareDelete(request.type(), request.id(), request.version()).origin(
				Engine.Operation.Origin.PRIMARY);
		indexShard.delete(delete);

		request.version(delete.version());

		if (request.refresh()) {
			try {
				indexShard.refresh(new Engine.Refresh(false));
			} catch (Exception e) {

			}
		}

		ShardDeleteResponse response = new ShardDeleteResponse(delete.version(), delete.notFound());
		return new PrimaryResponse<ShardDeleteResponse, ShardDeleteRequest>(shardRequest.request, response, null);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shardOperationOnReplica(cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction.ReplicaOperationRequest)
	 */
	@Override
	protected void shardOperationOnReplica(ReplicaOperationRequest shardRequest) {
		ShardDeleteRequest request = shardRequest.request;
		IndexShard indexShard = indicesService.indexServiceSafe(shardRequest.request.index()).shardSafe(
				shardRequest.shardId);
		Engine.Delete delete = indexShard.prepareDelete(request.type(), request.id(), request.version()).origin(
				Engine.Operation.Origin.REPLICA);
		indexShard.delete(delete);

		if (request.refresh()) {
			try {
				indexShard.refresh(new Engine.Refresh(false));
			} catch (Exception e) {

			}
		}

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shards(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ShardIterator shards(ClusterState clusterState, ShardDeleteRequest request) {
		GroupShardsIterator group = clusterService.operationRouting().broadcastDeleteShards(clusterService.state(),
				request.index());
		for (ShardIterator shardIt : group) {
			if (shardIt.shardId().id() == request.shardId()) {
				return shardIt;
			}
		}
		throw new RebirthIllegalStateException("No shards iterator found for shard [" + request.shardId() + "]");
	}
}
