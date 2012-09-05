/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportDeleteMappingAction.java 2012-7-6 14:29:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.mapping.delete;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.refresh.RefreshResponse;
import cn.com.rebirth.search.core.action.admin.indices.refresh.TransportRefreshAction;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryResponse;
import cn.com.rebirth.search.core.action.deletebyquery.TransportDeleteByQueryAction;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataMappingService;
import cn.com.rebirth.search.core.index.query.FilterBuilders;
import cn.com.rebirth.search.core.index.query.QueryBuilders;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportDeleteMappingAction.
 *
 * @author l.xue.nong
 */
public class TransportDeleteMappingAction extends
		TransportMasterNodeOperationAction<DeleteMappingRequest, DeleteMappingResponse> {

	/** The meta data mapping service. */
	private final MetaDataMappingService metaDataMappingService;

	/** The delete by query action. */
	private final TransportDeleteByQueryAction deleteByQueryAction;

	/** The refresh action. */
	private final TransportRefreshAction refreshAction;

	/**
	 * Instantiates a new transport delete mapping action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param metaDataMappingService the meta data mapping service
	 * @param deleteByQueryAction the delete by query action
	 * @param refreshAction the refresh action
	 */
	@Inject
	public TransportDeleteMappingAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool, MetaDataMappingService metaDataMappingService,
			TransportDeleteByQueryAction deleteByQueryAction, TransportRefreshAction refreshAction) {
		super(settings, transportService, clusterService, threadPool);
		this.metaDataMappingService = metaDataMappingService;
		this.deleteByQueryAction = deleteByQueryAction;
		this.refreshAction = refreshAction;
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
		return DeleteMappingAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected DeleteMappingRequest newRequest() {
		return new DeleteMappingRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected DeleteMappingResponse newResponse() {
		return new DeleteMappingResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#doExecute(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(DeleteMappingRequest request, ActionListener<DeleteMappingResponse> listener) {

		request.indices(clusterService.state().metaData().concreteIndices(request.indices()));
		super.doExecute(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#checkBlock(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClusterBlockException checkBlock(DeleteMappingRequest request, ClusterState state) {
		return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, request.indices());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected DeleteMappingResponse masterOperation(final DeleteMappingRequest request, final ClusterState state)
			throws RebirthException {

		final AtomicReference<Throwable> failureRef = new AtomicReference<Throwable>();
		final CountDownLatch latch = new CountDownLatch(1);
		deleteByQueryAction.execute(
				Requests.deleteByQueryRequest(request.indices()).query(
						QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
								FilterBuilders.typeFilter(request.type()))),
				new ActionListener<DeleteByQueryResponse>() {
					@Override
					public void onResponse(DeleteByQueryResponse deleteByQueryResponse) {
						refreshAction.execute(Requests.refreshRequest(request.indices()),
								new ActionListener<RefreshResponse>() {
									@Override
									public void onResponse(RefreshResponse refreshResponse) {
										metaDataMappingService.removeMapping(new MetaDataMappingService.RemoveRequest(
												request.indices(), request.type()));
										latch.countDown();
									}

									@Override
									public void onFailure(Throwable e) {
										metaDataMappingService.removeMapping(new MetaDataMappingService.RemoveRequest(
												request.indices(), request.type()));
										latch.countDown();
									}
								});
					}

					@Override
					public void onFailure(Throwable e) {
						failureRef.set(e);
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

		return new DeleteMappingResponse();
	}
}