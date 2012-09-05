/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportCreateIndexAction.java 2012-7-6 14:30:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.create;

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
import cn.com.rebirth.search.core.cluster.metadata.MetaDataCreateIndexService;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportCreateIndexAction.
 *
 * @author l.xue.nong
 */
public class TransportCreateIndexAction extends
		TransportMasterNodeOperationAction<CreateIndexRequest, CreateIndexResponse> {

	/** The create index service. */
	private final MetaDataCreateIndexService createIndexService;

	/**
	 * Instantiates a new transport create index action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param createIndexService the create index service
	 */
	@Inject
	public TransportCreateIndexAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool, MetaDataCreateIndexService createIndexService) {
		super(settings, transportService, clusterService, threadPool);
		this.createIndexService = createIndexService;
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
		return CreateIndexAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected CreateIndexRequest newRequest() {
		return new CreateIndexRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected CreateIndexResponse newResponse() {
		return new CreateIndexResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#checkBlock(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClusterBlockException checkBlock(CreateIndexRequest request, ClusterState state) {
		return state.blocks().indexBlockedException(ClusterBlockLevel.METADATA, request.index());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected CreateIndexResponse masterOperation(CreateIndexRequest request, ClusterState state)
			throws RebirthException {
		String cause = request.cause();
		if (cause.length() == 0) {
			cause = "api";
		}

		final AtomicReference<CreateIndexResponse> responseRef = new AtomicReference<CreateIndexResponse>();
		final AtomicReference<Throwable> failureRef = new AtomicReference<Throwable>();
		final CountDownLatch latch = new CountDownLatch(1);
		createIndexService.createIndex(
				new MetaDataCreateIndexService.Request(cause, request.index()).settings(request.settings())
						.mappings(request.mappings()).timeout(request.timeout()),
				new MetaDataCreateIndexService.Listener() {
					@Override
					public void onResponse(MetaDataCreateIndexService.Response response) {
						responseRef.set(new CreateIndexResponse(response.acknowledged()));
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
