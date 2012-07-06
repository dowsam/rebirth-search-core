/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportPutMappingAction.java 2012-3-29 15:01:31 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.mapping.put;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataMappingService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;


/**
 * The Class TransportPutMappingAction.
 *
 * @author l.xue.nong
 */
public class TransportPutMappingAction extends
		TransportMasterNodeOperationAction<PutMappingRequest, PutMappingResponse> {

	
	/** The meta data mapping service. */
	private final MetaDataMappingService metaDataMappingService;

	
	/**
	 * Instantiates a new transport put mapping action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param metaDataMappingService the meta data mapping service
	 */
	@Inject
	public TransportPutMappingAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool, MetaDataMappingService metaDataMappingService) {
		super(settings, transportService, clusterService, threadPool);
		this.metaDataMappingService = metaDataMappingService;
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
		return PutMappingAction.NAME;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected PutMappingRequest newRequest() {
		return new PutMappingRequest();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected PutMappingResponse newResponse() {
		return new PutMappingResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#doExecute(cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(PutMappingRequest request, ActionListener<PutMappingResponse> listener) {
		request.indices(clusterService.state().metaData().concreteIndices(request.indices()));
		super.doExecute(request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#checkBlock(cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest, cn.com.summall.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClusterBlockException checkBlock(PutMappingRequest request, ClusterState state) {
		return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, request.indices());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest, cn.com.summall.search.core.cluster.ClusterState)
	 */
	@Override
	protected PutMappingResponse masterOperation(PutMappingRequest request, ClusterState state)
			throws RestartException {
		ClusterState clusterState = clusterService.state();

		
		request.indices(clusterState.metaData().concreteIndices(request.indices()));

		final AtomicReference<PutMappingResponse> responseRef = new AtomicReference<PutMappingResponse>();
		final AtomicReference<Throwable> failureRef = new AtomicReference<Throwable>();
		final CountDownLatch latch = new CountDownLatch(1);
		metaDataMappingService.putMapping(new MetaDataMappingService.PutRequest(request.indices(), request.type(),
				request.source()).ignoreConflicts(request.ignoreConflicts()).timeout(request.timeout()),
				new MetaDataMappingService.Listener() {
					@Override
					public void onResponse(MetaDataMappingService.Response response) {
						responseRef.set(new PutMappingResponse(response.acknowledged()));
						latch.countDown();
					}

					@Override
					public void onFailure(Throwable t) {
						failureRef.set(t);
						latch.countDown();
					}
				});

		try {
			latch.await();
		} catch (InterruptedException e) {
			failureRef.set(e);
		}

		if (failureRef.get() != null) {
			if (failureRef.get() instanceof RestartException) {
				throw (RestartException) failureRef.get();
			} else {
				throw new RestartException(failureRef.get().getMessage(), failureRef.get());
			}
		}

		return responseRef.get();
	}
}