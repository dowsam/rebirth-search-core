/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportOpenIndexAction.java 2012-7-6 14:28:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.open;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataStateIndexService;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportOpenIndexAction.
 *
 * @author l.xue.nong
 */
public class TransportOpenIndexAction extends TransportMasterNodeOperationAction<OpenIndexRequest, OpenIndexResponse> {

	/** The state index service. */
	private final MetaDataStateIndexService stateIndexService;

	/**
	 * Instantiates a new transport open index action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param stateIndexService the state index service
	 */
	@Inject
	public TransportOpenIndexAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool, MetaDataStateIndexService stateIndexService) {
		super(settings, transportService, clusterService, threadPool);
		this.stateIndexService = stateIndexService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.MANAGEMENT;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return OpenIndexAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected OpenIndexRequest newRequest() {
		return new OpenIndexRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected OpenIndexResponse newResponse() {
		return new OpenIndexResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#checkBlock(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClusterBlockException checkBlock(OpenIndexRequest request, ClusterState state) {
		request.index(clusterService.state().metaData().concreteIndex(request.index()));
		return state.blocks().indexBlockedException(ClusterBlockLevel.METADATA, request.index());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected OpenIndexResponse masterOperation(OpenIndexRequest request, ClusterState state) throws RebirthException {
		final AtomicReference<OpenIndexResponse> responseRef = new AtomicReference<OpenIndexResponse>();
		final AtomicReference<Throwable> failureRef = new AtomicReference<Throwable>();
		final CountDownLatch latch = new CountDownLatch(1);
		stateIndexService.openIndex(new MetaDataStateIndexService.Request(request.index()).timeout(request.timeout()),
				new MetaDataStateIndexService.Listener() {
					@Override
					public void onResponse(MetaDataStateIndexService.Response response) {
						responseRef.set(new OpenIndexResponse(response.acknowledged()));
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
			if (failureRef.get() instanceof RebirthException) {
				throw (RebirthException) failureRef.get();
			} else {
				throw new RebirthException(failureRef.get().getMessage(), failureRef.get());
			}
		}

		return responseRef.get();
	}
}
