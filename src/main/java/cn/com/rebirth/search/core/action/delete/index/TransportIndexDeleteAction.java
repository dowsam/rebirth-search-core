/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportIndexDeleteAction.java 2012-3-29 15:02:22 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.delete.index;

import java.util.ArrayList;
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
 * The Class TransportIndexDeleteAction.
 *
 * @author l.xue.nong
 */
public class TransportIndexDeleteAction
		extends
		TransportIndexReplicationOperationAction<IndexDeleteRequest, IndexDeleteResponse, ShardDeleteRequest, ShardDeleteRequest, ShardDeleteResponse> {

	
	/**
	 * Instantiates a new transport index delete action.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param threadPool the thread pool
	 * @param deleteAction the delete action
	 */
	@Inject
	public TransportIndexDeleteAction(Settings settings, ClusterService clusterService,
			TransportService transportService, ThreadPool threadPool, TransportShardDeleteAction deleteAction) {
		super(settings, transportService, clusterService, threadPool, deleteAction);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.TransportIndexReplicationOperationAction#newRequestInstance()
	 */
	@Override
	protected IndexDeleteRequest newRequestInstance() {
		return new IndexDeleteRequest();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.TransportIndexReplicationOperationAction#newResponseInstance(cn.com.summall.search.core.action.support.replication.IndexReplicationOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray)
	 */
	@Override
	protected IndexDeleteResponse newResponseInstance(IndexDeleteRequest request, AtomicReferenceArray shardsResponses) {
		int successfulShards = 0;
		int failedShards = 0;
		ArrayList<ShardDeleteResponse> responses = new ArrayList<ShardDeleteResponse>();
		for (int i = 0; i < shardsResponses.length(); i++) {
			if (shardsResponses.get(i) == null) {
				failedShards++;
			} else {
				responses.add((ShardDeleteResponse) shardsResponses.get(i));
				successfulShards++;
			}
		}
		return new IndexDeleteResponse(request.index(), successfulShards, failedShards,
				responses.toArray(new ShardDeleteResponse[responses.size()]));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.TransportIndexReplicationOperationAction#accumulateExceptions()
	 */
	@Override
	protected boolean accumulateExceptions() {
		return false;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.TransportIndexReplicationOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return "indices/index/delete";
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.TransportIndexReplicationOperationAction#checkGlobalBlock(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.replication.IndexReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, IndexDeleteRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.WRITE);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.TransportIndexReplicationOperationAction#checkRequestBlock(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.replication.IndexReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, IndexDeleteRequest request) {
		return state.blocks().indexBlockedException(ClusterBlockLevel.WRITE, request.index());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.TransportIndexReplicationOperationAction#shards(cn.com.summall.search.core.action.support.replication.IndexReplicationOperationRequest)
	 */
	@Override
	protected GroupShardsIterator shards(IndexDeleteRequest request) {
		return clusterService.operationRouting().broadcastDeleteShards(clusterService.state(), request.index());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.TransportIndexReplicationOperationAction#newShardRequestInstance(cn.com.summall.search.core.action.support.replication.IndexReplicationOperationRequest, int)
	 */
	@Override
	protected ShardDeleteRequest newShardRequestInstance(IndexDeleteRequest request, int shardId) {
		return new ShardDeleteRequest(request, shardId);
	}
}