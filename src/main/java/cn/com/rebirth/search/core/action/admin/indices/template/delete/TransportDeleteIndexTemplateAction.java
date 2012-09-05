/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportDeleteIndexTemplateAction.java 2012-7-6 14:29:13 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.template.delete;

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
import cn.com.rebirth.search.core.cluster.metadata.MetaDataIndexTemplateService;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportDeleteIndexTemplateAction.
 *
 * @author l.xue.nong
 */
public class TransportDeleteIndexTemplateAction extends
		TransportMasterNodeOperationAction<DeleteIndexTemplateRequest, DeleteIndexTemplateResponse> {

	/** The index template service. */
	private final MetaDataIndexTemplateService indexTemplateService;

	/**
	 * Instantiates a new transport delete index template action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param indexTemplateService the index template service
	 */
	@Inject
	public TransportDeleteIndexTemplateAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool, MetaDataIndexTemplateService indexTemplateService) {
		super(settings, transportService, clusterService, threadPool);
		this.indexTemplateService = indexTemplateService;
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
		return DeleteIndexTemplateAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected DeleteIndexTemplateRequest newRequest() {
		return new DeleteIndexTemplateRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected DeleteIndexTemplateResponse newResponse() {
		return new DeleteIndexTemplateResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#checkBlock(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClusterBlockException checkBlock(DeleteIndexTemplateRequest request, ClusterState state) {
		return state.blocks().indexBlockedException(ClusterBlockLevel.METADATA, "");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected DeleteIndexTemplateResponse masterOperation(DeleteIndexTemplateRequest request, ClusterState state)
			throws RebirthException {
		final AtomicReference<DeleteIndexTemplateResponse> responseRef = new AtomicReference<DeleteIndexTemplateResponse>();
		final AtomicReference<Throwable> failureRef = new AtomicReference<Throwable>();
		final CountDownLatch latch = new CountDownLatch(1);

		indexTemplateService.removeTemplate(new MetaDataIndexTemplateService.RemoveRequest(request.name()),
				new MetaDataIndexTemplateService.RemoveListener() {
					@Override
					public void onResponse(MetaDataIndexTemplateService.RemoveResponse response) {
						responseRef.set(new DeleteIndexTemplateResponse(response.acknowledged()));
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
