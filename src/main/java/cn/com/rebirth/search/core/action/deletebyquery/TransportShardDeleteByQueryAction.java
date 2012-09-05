/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportShardDeleteByQueryAction.java 2012-7-6 14:29:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.deletebyquery;

import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
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
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportShardDeleteByQueryAction.
 *
 * @author l.xue.nong
 */
public class TransportShardDeleteByQueryAction
		extends
		TransportShardReplicationOperationAction<ShardDeleteByQueryRequest, ShardDeleteByQueryRequest, ShardDeleteByQueryResponse> {

	/**
	 * Instantiates a new transport shard delete by query action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param indicesService the indices service
	 * @param threadPool the thread pool
	 * @param shardStateAction the shard state action
	 */
	@Inject
	public TransportShardDeleteByQueryAction(Settings settings, TransportService transportService,
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
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.INDEX;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newRequestInstance()
	 */
	@Override
	protected ShardDeleteByQueryRequest newRequestInstance() {
		return new ShardDeleteByQueryRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newReplicaRequestInstance()
	 */
	@Override
	protected ShardDeleteByQueryRequest newReplicaRequestInstance() {
		return new ShardDeleteByQueryRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newResponseInstance()
	 */
	@Override
	protected ShardDeleteByQueryResponse newResponseInstance() {
		return new ShardDeleteByQueryResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return DeleteByQueryAction.NAME + "/shard";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, ShardDeleteByQueryRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.WRITE);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, ShardDeleteByQueryRequest request) {
		return state.blocks().indexBlockedException(ClusterBlockLevel.WRITE, request.index());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shardOperationOnPrimary(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction.PrimaryOperationRequest)
	 */
	@Override
	protected PrimaryResponse<ShardDeleteByQueryResponse, ShardDeleteByQueryRequest> shardOperationOnPrimary(
			ClusterState clusterState, PrimaryOperationRequest shardRequest) {
		ShardDeleteByQueryRequest request = shardRequest.request;
		IndexShard indexShard = indicesService.indexServiceSafe(shardRequest.request.index()).shardSafe(
				shardRequest.shardId);
		Engine.DeleteByQuery deleteByQuery = indexShard.prepareDeleteByQuery(request.querySource(),
				request.filteringAliases(), request.types());
		indexShard.deleteByQuery(deleteByQuery);
		return new PrimaryResponse<ShardDeleteByQueryResponse, ShardDeleteByQueryRequest>(shardRequest.request,
				new ShardDeleteByQueryResponse(), null);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shardOperationOnReplica(cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction.ReplicaOperationRequest)
	 */
	@Override
	protected void shardOperationOnReplica(ReplicaOperationRequest shardRequest) {
		ShardDeleteByQueryRequest request = shardRequest.request;
		IndexShard indexShard = indicesService.indexServiceSafe(shardRequest.request.index()).shardSafe(
				shardRequest.shardId);
		Engine.DeleteByQuery deleteByQuery = indexShard.prepareDeleteByQuery(request.querySource(),
				request.filteringAliases(), request.types());
		indexShard.deleteByQuery(deleteByQuery);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shards(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ShardIterator shards(ClusterState clusterState, ShardDeleteByQueryRequest request) {
		GroupShardsIterator group = clusterService.operationRouting().deleteByQueryShards(clusterService.state(),
				request.index(), request.routing());
		for (ShardIterator shardIt : group) {
			if (shardIt.shardId().id() == request.shardId()) {
				return shardIt;
			}
		}
		throw new RebirthIllegalStateException("No shards iterator found for shard [" + request.shardId() + "]");
	}
}
