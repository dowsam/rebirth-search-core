/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportClusterUpdateSettingsAction.java 2012-7-6 14:29:03 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.settings;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.ProcessedClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.block.ClusterBlocks;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.routing.allocation.AllocationService;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportClusterUpdateSettingsAction.
 *
 * @author l.xue.nong
 */
public class TransportClusterUpdateSettingsAction extends
		TransportMasterNodeOperationAction<ClusterUpdateSettingsRequest, ClusterUpdateSettingsResponse> {

	/** The allocation service. */
	private final AllocationService allocationService;

	/**
	 * Instantiates a new transport cluster update settings action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param allocationService the allocation service
	 */
	@Inject
	public TransportClusterUpdateSettingsAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool, AllocationService allocationService) {
		super(settings, transportService, clusterService, threadPool);
		this.allocationService = allocationService;
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
		return ClusterUpdateSettingsAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected ClusterUpdateSettingsRequest newRequest() {
		return new ClusterUpdateSettingsRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected ClusterUpdateSettingsResponse newResponse() {
		return new ClusterUpdateSettingsResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClusterUpdateSettingsResponse masterOperation(final ClusterUpdateSettingsRequest request,
			ClusterState state) throws RebirthException {
		final AtomicReference<Throwable> failureRef = new AtomicReference<Throwable>();
		final CountDownLatch latch = new CountDownLatch(1);

		clusterService.submitStateUpdateTask("cluster_update_settings", new ProcessedClusterStateUpdateTask() {
			@Override
			public ClusterState execute(ClusterState currentState) {
				try {
					boolean changed = false;
					ImmutableSettings.Builder transientSettings = ImmutableSettings.settingsBuilder();
					transientSettings.put(currentState.metaData().transientSettings());
					for (Map.Entry<String, String> entry : request.transientSettings().getAsMap().entrySet()) {
						if (MetaData.hasDynamicSetting(entry.getKey()) || entry.getKey().startsWith("logger.")) {
							transientSettings.put(entry.getKey(), entry.getValue());
							changed = true;
						} else {
							logger.warn("ignoring transient setting [{}], not dynamically updateable", entry.getKey());
						}
					}

					ImmutableSettings.Builder persistentSettings = ImmutableSettings.settingsBuilder();
					persistentSettings.put(currentState.metaData().persistentSettings());
					for (Map.Entry<String, String> entry : request.persistentSettings().getAsMap().entrySet()) {
						if (MetaData.hasDynamicSetting(entry.getKey()) || entry.getKey().startsWith("logger.")) {
							changed = true;
							persistentSettings.put(entry.getKey(), entry.getValue());
						} else {
							logger.warn("ignoring persistent setting [{}], not dynamically updateable", entry.getKey());
						}
					}

					if (!changed) {
						latch.countDown();
						return currentState;
					}

					MetaData.Builder metaData = MetaData.builder().metaData(currentState.metaData())
							.persistentSettings(persistentSettings.build())
							.transientSettings(transientSettings.build());

					ClusterBlocks.Builder blocks = ClusterBlocks.builder().blocks(currentState.blocks());
					boolean updatedReadOnly = metaData.persistentSettings().getAsBoolean(MetaData.SETTING_READ_ONLY,
							false)
							|| metaData.transientSettings().getAsBoolean(MetaData.SETTING_READ_ONLY, false);
					if (updatedReadOnly) {
						blocks.addGlobalBlock(MetaData.CLUSTER_READ_ONLY_BLOCK);
					} else {
						blocks.removeGlobalBlock(MetaData.CLUSTER_READ_ONLY_BLOCK);
					}

					return ClusterState.builder().state(currentState).metaData(metaData).blocks(blocks).build();
				} catch (Exception e) {
					latch.countDown();
					logger.warn("failed to update cluster settings", e);
					return currentState;
				} finally {

				}
			}

			@Override
			public void clusterStateProcessed(ClusterState clusterState) {

				clusterService.submitStateUpdateTask("reroute_after_cluster_update_settings",
						new ClusterStateUpdateTask() {
							@Override
							public ClusterState execute(ClusterState currentState) {
								try {

									RoutingAllocation.Result routingResult = allocationService.reroute(currentState);
									return ClusterState.newClusterStateBuilder().state(currentState)
											.routingResult(routingResult).build();
								} finally {
									latch.countDown();
								}
							}
						});
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

		return new ClusterUpdateSettingsResponse();
	}
}
