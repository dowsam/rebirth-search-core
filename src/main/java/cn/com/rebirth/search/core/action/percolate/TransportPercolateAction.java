/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportPercolateAction.java 2012-3-29 15:02:54 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.percolate;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.support.single.custom.TransportSingleCustomOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.routing.ShardsIterator;
import cn.com.rebirth.search.core.index.percolator.PercolatorExecutor;
import cn.com.rebirth.search.core.index.percolator.PercolatorService;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;


/**
 * The Class TransportPercolateAction.
 *
 * @author l.xue.nong
 */
public class TransportPercolateAction extends TransportSingleCustomOperationAction<PercolateRequest, PercolateResponse> {

	
	/** The indices service. */
	private final IndicesService indicesService;

	
	/**
	 * Instantiates a new transport percolate action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 */
	@Inject
	public TransportPercolateAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			TransportService transportService, IndicesService indicesService) {
		super(settings, threadPool, clusterService, transportService);
		this.indicesService = indicesService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.single.custom.TransportSingleCustomOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.PERCOLATE;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.single.custom.TransportSingleCustomOperationAction#newRequest()
	 */
	@Override
	protected PercolateRequest newRequest() {
		return new PercolateRequest();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.single.custom.TransportSingleCustomOperationAction#newResponse()
	 */
	@Override
	protected PercolateResponse newResponse() {
		return new PercolateResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.single.custom.TransportSingleCustomOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return PercolateAction.NAME;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.single.custom.TransportSingleCustomOperationAction#checkGlobalBlock(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.single.custom.SingleCustomOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, PercolateRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.READ);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.single.custom.TransportSingleCustomOperationAction#checkRequestBlock(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.single.custom.SingleCustomOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, PercolateRequest request) {
		request.index(state.metaData().concreteIndex(request.index()));
		return state.blocks().indexBlockedException(ClusterBlockLevel.READ, request.index());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.single.custom.TransportSingleCustomOperationAction#shards(cn.com.summall.search.core.cluster.ClusterState, cn.com.summall.search.core.action.support.single.custom.SingleCustomOperationRequest)
	 */
	@Override
	protected ShardsIterator shards(ClusterState clusterState, PercolateRequest request) {
		return clusterState.routingTable().index(request.index()).randomAllActiveShardsIt();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.single.custom.TransportSingleCustomOperationAction#shardOperation(cn.com.summall.search.core.action.support.single.custom.SingleCustomOperationRequest, int)
	 */
	@Override
	protected PercolateResponse shardOperation(PercolateRequest request, int shardId) throws RestartException {
		IndexService indexService = indicesService.indexServiceSafe(request.index());
		PercolatorService percolatorService = indexService.percolateService();

		PercolatorExecutor.Response percolate = percolatorService.percolate(new PercolatorExecutor.SourceRequest(
				request.type(), request.underlyingSource(), request.underlyingSourceOffset(), request
						.underlyingSourceLength()));
		return new PercolateResponse(percolate.matches());
	}
}
