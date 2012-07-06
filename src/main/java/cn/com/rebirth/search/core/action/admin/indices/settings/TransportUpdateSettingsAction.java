/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportUpdateSettingsAction.java 2012-7-6 14:30:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.settings;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataUpdateSettingsService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportUpdateSettingsAction.
 *
 * @author l.xue.nong
 */
public class TransportUpdateSettingsAction extends
		TransportMasterNodeOperationAction<UpdateSettingsRequest, UpdateSettingsResponse> {

	/** The update settings service. */
	private final MetaDataUpdateSettingsService updateSettingsService;

	/**
	 * Instantiates a new transport update settings action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param updateSettingsService the update settings service
	 */
	@Inject
	public TransportUpdateSettingsAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool, MetaDataUpdateSettingsService updateSettingsService) {
		super(settings, transportService, clusterService, threadPool);
		this.updateSettingsService = updateSettingsService;
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
		return UpdateSettingsAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected UpdateSettingsRequest newRequest() {
		return new UpdateSettingsRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected UpdateSettingsResponse newResponse() {
		return new UpdateSettingsResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected UpdateSettingsResponse masterOperation(UpdateSettingsRequest request, ClusterState state)
			throws RebirthException {
		final AtomicReference<Throwable> failureRef = new AtomicReference<Throwable>();
		final CountDownLatch latch = new CountDownLatch(1);

		updateSettingsService.updateSettings(request.settings(), request.indices(),
				new MetaDataUpdateSettingsService.Listener() {
					@Override
					public void onSuccess() {
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

		return new UpdateSettingsResponse();
	}
}
