/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportClusterRerouteAction.java 2012-7-6 14:30:00 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.reroute;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ProcessedClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.routing.allocation.AllocationService;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportClusterRerouteAction.
 *
 * @author l.xue.nong
 */
public class TransportClusterRerouteAction extends
		TransportMasterNodeOperationAction<ClusterRerouteRequest, ClusterRerouteResponse> {

	/** The allocation service. */
	private final AllocationService allocationService;

	/**
	 * Instantiates a new transport cluster reroute action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param allocationService the allocation service
	 */
	@Inject
	public TransportClusterRerouteAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool, AllocationService allocationService) {
		super(settings, transportService, clusterService, threadPool);
		this.allocationService = allocationService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.GENERIC;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return ClusterRerouteAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected ClusterRerouteRequest newRequest() {
		return new ClusterRerouteRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected ClusterRerouteResponse newResponse() {
		return new ClusterRerouteResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClusterRerouteResponse masterOperation(ClusterRerouteRequest request, ClusterState state)
			throws RebirthException {
		final AtomicReference<Throwable> failureRef = new AtomicReference<Throwable>();
		final CountDownLatch latch = new CountDownLatch(1);

		clusterService.submitStateUpdateTask("cluster_reroute (api)", new ProcessedClusterStateUpdateTask() {
			@Override
			public ClusterState execute(ClusterState currentState) {
				try {
					RoutingAllocation.Result routingResult = allocationService.reroute(currentState);
					return ClusterState.newClusterStateBuilder().state(currentState).routingResult(routingResult)
							.build();
				} catch (Exception e) {
					latch.countDown();
					logger.warn("failed to reroute", e);
					return currentState;
				} finally {

				}
			}

			@Override
			public void clusterStateProcessed(ClusterState clusterState) {
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

		return new ClusterRerouteResponse();

	}
}