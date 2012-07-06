/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportDeleteIndexAction.java 2012-7-6 14:29:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.delete;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingRequest;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingResponse;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.TransportDeleteMappingAction;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataDeleteIndexService;
import cn.com.rebirth.search.core.index.percolator.PercolatorService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportDeleteIndexAction.
 *
 * @author l.xue.nong
 */
public class TransportDeleteIndexAction extends
		TransportMasterNodeOperationAction<DeleteIndexRequest, DeleteIndexResponse> {

	/** The delete index service. */
	private final MetaDataDeleteIndexService deleteIndexService;

	/** The delete mapping action. */
	private final TransportDeleteMappingAction deleteMappingAction;

	/** The disable delete all indices. */
	private final boolean disableDeleteAllIndices;

	/**
	 * Instantiates a new transport delete index action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param deleteIndexService the delete index service
	 * @param deleteMappingAction the delete mapping action
	 */
	@Inject
	public TransportDeleteIndexAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool, MetaDataDeleteIndexService deleteIndexService,
			TransportDeleteMappingAction deleteMappingAction) {
		super(settings, transportService, clusterService, threadPool);
		this.deleteIndexService = deleteIndexService;
		this.deleteMappingAction = deleteMappingAction;

		this.disableDeleteAllIndices = settings.getAsBoolean("action.disable_delete_all_indices", false);
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
		return DeleteIndexAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected DeleteIndexRequest newRequest() {
		return new DeleteIndexRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected DeleteIndexResponse newResponse() {
		return new DeleteIndexResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#doExecute(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(DeleteIndexRequest request, ActionListener<DeleteIndexResponse> listener) {
		if (disableDeleteAllIndices && (request.indices() == null || request.indices().length == 0)) {
			throw new RebirthIllegalArgumentException("deleting all indices is disabled");
		}
		request.indices(clusterService.state().metaData().concreteIndices(request.indices()));
		super.doExecute(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#checkBlock(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClusterBlockException checkBlock(DeleteIndexRequest request, ClusterState state) {
		return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, request.indices());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected DeleteIndexResponse masterOperation(DeleteIndexRequest request, final ClusterState state)
			throws RebirthException {
		if (request.indices().length == 0) {
			return new DeleteIndexResponse(true);
		}
		final AtomicReference<DeleteIndexResponse> responseRef = new AtomicReference<DeleteIndexResponse>();
		final AtomicReference<Throwable> failureRef = new AtomicReference<Throwable>();
		final CountDownLatch latch = new CountDownLatch(request.indices().length);
		for (final String index : request.indices()) {
			deleteIndexService.deleteIndex(new MetaDataDeleteIndexService.Request(index).timeout(request.timeout()),
					new MetaDataDeleteIndexService.Listener() {
						@Override
						public void onResponse(MetaDataDeleteIndexService.Response response) {
							responseRef.set(new DeleteIndexResponse(response.acknowledged()));

							IndexMetaData percolatorMetaData = state.metaData().index(PercolatorService.INDEX_NAME);
							if (percolatorMetaData != null && percolatorMetaData.mappings().containsKey(index)) {
								deleteMappingAction.execute(
										new DeleteMappingRequest(PercolatorService.INDEX_NAME).type(index),
										new ActionListener<DeleteMappingResponse>() {
											@Override
											public void onResponse(DeleteMappingResponse deleteMappingResponse) {
												latch.countDown();
											}

											@Override
											public void onFailure(Throwable e) {
												latch.countDown();
											}
										});
							} else {
								latch.countDown();
							}
						}

						@Override
						public void onFailure(Throwable t) {
							failureRef.set(t);
							latch.countDown();
						}
					});
		}

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
