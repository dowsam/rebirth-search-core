/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportIndicesExistsAction.java 2012-3-29 15:02:26 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.exists;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;


/**
 * The Class TransportIndicesExistsAction.
 *
 * @author l.xue.nong
 */
public class TransportIndicesExistsAction extends
		TransportMasterNodeOperationAction<IndicesExistsRequest, IndicesExistsResponse> {

	
	/**
	 * Instantiates a new transport indices exists action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 */
	@Inject
	public TransportIndicesExistsAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool) {
		super(settings, transportService, clusterService, threadPool);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.MANAGEMENT;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return IndicesExistsAction.NAME;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected IndicesExistsRequest newRequest() {
		return new IndicesExistsRequest();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected IndicesExistsResponse newResponse() {
		return new IndicesExistsResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#doExecute(cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(IndicesExistsRequest request, ActionListener<IndicesExistsResponse> listener) {
		
		
		super.doExecute(request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#checkBlock(cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest, cn.com.summall.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClusterBlockException checkBlock(IndicesExistsRequest request, ClusterState state) {
		return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, request.indices());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest, cn.com.summall.search.core.cluster.ClusterState)
	 */
	@Override
	protected IndicesExistsResponse masterOperation(IndicesExistsRequest request, ClusterState state)
			throws RestartException {
		boolean exists = true;
		for (String index : request.indices()) {
			if (!state.metaData().hasConcreteIndex(index)) {
				exists = false;
			}
		}
		return new IndicesExistsResponse(exists);
	}
}
