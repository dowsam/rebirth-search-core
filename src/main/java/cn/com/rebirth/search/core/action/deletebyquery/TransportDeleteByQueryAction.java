/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportDeleteByQueryAction.java 2012-7-6 14:30:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.deletebyquery;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.support.replication.TransportIndicesReplicationOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportDeleteByQueryAction.
 *
 * @author l.xue.nong
 */
public class TransportDeleteByQueryAction
		extends
		TransportIndicesReplicationOperationAction<DeleteByQueryRequest, DeleteByQueryResponse, IndexDeleteByQueryRequest, IndexDeleteByQueryResponse, ShardDeleteByQueryRequest, ShardDeleteByQueryRequest, ShardDeleteByQueryResponse> {

	/**
	 * Instantiates a new transport delete by query action.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param threadPool the thread pool
	 * @param indexDeleteByQueryAction the index delete by query action
	 */
	@Inject
	public TransportDeleteByQueryAction(Settings settings, ClusterService clusterService,
			TransportService transportService, ThreadPool threadPool,
			TransportIndexDeleteByQueryAction indexDeleteByQueryAction) {
		super(settings, transportService, clusterService, threadPool, indexDeleteByQueryAction);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndicesReplicationOperationAction#newRequestInstance()
	 */
	@Override
	protected DeleteByQueryRequest newRequestInstance() {
		return new DeleteByQueryRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndicesReplicationOperationAction#newResponseInstance(cn.com.rebirth.search.core.action.support.replication.IndicesReplicationOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray)
	 */
	@Override
	protected DeleteByQueryResponse newResponseInstance(DeleteByQueryRequest request,
			AtomicReferenceArray indexResponses) {
		DeleteByQueryResponse response = new DeleteByQueryResponse();
		for (int i = 0; i < indexResponses.length(); i++) {
			IndexDeleteByQueryResponse indexResponse = (IndexDeleteByQueryResponse) indexResponses.get(i);
			if (indexResponse != null) {
				response.indices().put(indexResponse.index(), indexResponse);
			}
		}
		return response;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndicesReplicationOperationAction#accumulateExceptions()
	 */
	@Override
	protected boolean accumulateExceptions() {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndicesReplicationOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return DeleteByQueryAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndicesReplicationOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.IndicesReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, DeleteByQueryRequest replicationPingRequest) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.READ);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndicesReplicationOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.IndicesReplicationOperationRequest, java.lang.String[])
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, DeleteByQueryRequest replicationPingRequest,
			String[] concreteIndices) {
		return state.blocks().indicesBlockedException(ClusterBlockLevel.WRITE, concreteIndices);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportIndicesReplicationOperationAction#newIndexRequestInstance(cn.com.rebirth.search.core.action.support.replication.IndicesReplicationOperationRequest, java.lang.String, java.util.Set)
	 */
	@Override
	protected IndexDeleteByQueryRequest newIndexRequestInstance(DeleteByQueryRequest request, String index,
			Set<String> routing) {
		String[] filteringAliases = clusterService.state().metaData().filteringAliases(index, request.indices());
		return new IndexDeleteByQueryRequest(request, index, routing, filteringAliases);
	}
}