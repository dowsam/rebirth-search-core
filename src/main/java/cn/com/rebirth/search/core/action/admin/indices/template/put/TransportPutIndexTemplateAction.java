/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportPutIndexTemplateAction.java 2012-7-6 14:28:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.template.put;

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
 * The Class TransportPutIndexTemplateAction.
 *
 * @author l.xue.nong
 */
public class TransportPutIndexTemplateAction extends
		TransportMasterNodeOperationAction<PutIndexTemplateRequest, PutIndexTemplateResponse> {

	/** The index template service. */
	private final MetaDataIndexTemplateService indexTemplateService;

	/**
	 * Instantiates a new transport put index template action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param indexTemplateService the index template service
	 */
	@Inject
	public TransportPutIndexTemplateAction(Settings settings, TransportService transportService,
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
		return PutIndexTemplateAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected PutIndexTemplateRequest newRequest() {
		return new PutIndexTemplateRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected PutIndexTemplateResponse newResponse() {
		return new PutIndexTemplateResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#checkBlock(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClusterBlockException checkBlock(PutIndexTemplateRequest request, ClusterState state) {
		return state.blocks().indexBlockedException(ClusterBlockLevel.METADATA, "");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected PutIndexTemplateResponse masterOperation(PutIndexTemplateRequest request, ClusterState state)
			throws RebirthException {
		String cause = request.cause();
		if (cause.length() == 0) {
			cause = "api";
		}

		final AtomicReference<PutIndexTemplateResponse> responseRef = new AtomicReference<PutIndexTemplateResponse>();
		final AtomicReference<Throwable> failureRef = new AtomicReference<Throwable>();
		final CountDownLatch latch = new CountDownLatch(1);
		indexTemplateService.putTemplate(
				new MetaDataIndexTemplateService.PutRequest(request.cause(), request.name())
						.template(request.template()).order(request.order()).settings(request.settings())
						.mappings(request.mappings()).create(request.create()),

				new MetaDataIndexTemplateService.PutListener() {
					@Override
					public void onResponse(MetaDataIndexTemplateService.PutResponse response) {
						responseRef.set(new PutIndexTemplateResponse(response.acknowledged()));
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
