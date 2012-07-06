/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportIndexDeleteByQueryAction.java 2012-7-6 14:29:41 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.deletebyquery;

import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.support.replication.TransportIndexReplicationOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.routing.GroupShardsIterator;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportIndexDeleteByQueryAction.
 *
 * @author l.xue.nong
 */
public class TransportIndexDeleteByQueryAction
		extends
		TransportIndexReplicationOperationAction<IndexDeleteByQueryRequest, IndexDeleteByQueryResponse, ShardDeleteByQueryRequest, ShardDeleteByQueryRequest, ShardDeleteByQueryResponse> {

	/**
	 * Instantiates a new transport index delete by query action.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param threadPool the thread pool
	 * @param shardDeleteByQueryAction the shard delete by query action
	 */
	@Inject
	public TransportIndexDeleteByQueryAction(Settings settings, ClusterService clusterService,
			TransportService transportService, ThreadPool threadPool,
			TransportShardDeleteByQueryAction shardDeleteByQueryAction) {
		super(settings, transportService, clusterService, threadPool, shardDeleteByQueryAction);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndexReplicationOperationAction#newRequestInstance()
	 */
	@Override
	protected IndexDeleteByQueryRequest newRequestInstance() {
		return new IndexDeleteByQueryRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndexReplicationOperationAction#newResponseInstance(cn.com.rebirth.search.core.action.support.replication.IndexReplicationOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray)
	 */
	@Override
	protected IndexDeleteByQueryResponse newResponseInstance(IndexDeleteByQueryRequest request,
			AtomicReferenceArray shardsResponses) {
		int successfulShards = 0;
		int failedShards = 0;
		for (int i = 0; i < shardsResponses.length(); i++) {
			if (shardsResponses.get(i) == null) {
				failedShards++;
			} else {
				successfulShards++;
			}
		}
		return new IndexDeleteByQueryResponse(request.index(), successfulShards, failedShards);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndexReplicationOperationAction#accumulateExceptions()
	 */
	@Override
	protected boolean accumulateExceptions() {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndexReplicationOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return DeleteByQueryAction.NAME + "/index";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndexReplicationOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.IndexReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, IndexDeleteByQueryRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.WRITE);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndexReplicationOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.IndexReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, IndexDeleteByQueryRequest request) {
		return state.blocks().indexBlockedException(ClusterBlockLevel.WRITE, request.index());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndexReplicationOperationAction#shards(cn.com.rebirth.search.core.action.support.replication.IndexReplicationOperationRequest)
	 */
	@Override
	protected GroupShardsIterator shards(IndexDeleteByQueryRequest request) {
		return clusterService.operationRouting().deleteByQueryShards(clusterService.state(), request.index(),
				request.routing());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndexReplicationOperationAction#newShardRequestInstance(cn.com.rebirth.search.core.action.support.replication.IndexReplicationOperationRequest, int)
	 */
	@Override
	protected ShardDeleteByQueryRequest newShardRequestInstance(IndexDeleteByQueryRequest request, int shardId) {
		return new ShardDeleteByQueryRequest(request, shardId);
	}
}