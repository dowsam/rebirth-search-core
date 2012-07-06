/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RoutingService.java 2012-7-6 14:30:05 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

import java.util.concurrent.Future;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;
import cn.com.rebirth.search.core.cluster.ClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.routing.allocation.AllocationService;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

/**
 * The Class RoutingService.
 *
 * @author l.xue.nong
 */
public class RoutingService extends AbstractLifecycleComponent<RoutingService> implements ClusterStateListener {

	/** The Constant CLUSTER_UPDATE_TASK_SOURCE. */
	private static final String CLUSTER_UPDATE_TASK_SOURCE = "routing-table-updater";

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The allocation service. */
	private final AllocationService allocationService;

	/** The schedule. */
	private final TimeValue schedule;

	/** The routing table dirty. */
	private volatile boolean routingTableDirty = false;

	/** The scheduled routing table future. */
	private volatile Future scheduledRoutingTableFuture;

	/**
	 * Instantiates a new routing service.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param allocationService the allocation service
	 */
	@Inject
	public RoutingService(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			AllocationService allocationService) {
		super(settings);
		this.threadPool = threadPool;
		this.clusterService = clusterService;
		this.allocationService = allocationService;
		this.schedule = componentSettings.getAsTime("schedule", TimeValue.timeValueSeconds(10));
		clusterService.addFirst(this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
		if (scheduledRoutingTableFuture != null) {
			scheduledRoutingTableFuture.cancel(true);
			scheduledRoutingTableFuture = null;
		}
		clusterService.remove(this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.ClusterStateListener#clusterChanged(cn.com.rebirth.search.core.cluster.ClusterChangedEvent)
	 */
	@Override
	public void clusterChanged(ClusterChangedEvent event) {
		if (event.source().equals(CLUSTER_UPDATE_TASK_SOURCE)) {

			return;
		}
		if (event.state().nodes().localNodeMaster()) {

			if (scheduledRoutingTableFuture == null) {

				routingTableDirty = true;
				scheduledRoutingTableFuture = threadPool.scheduleWithFixedDelay(new RoutingTableUpdater(), schedule);
			}
			if (event.nodesRemoved()) {

				routingTableDirty = true;
				reroute();

			} else {
				if (event.nodesAdded()) {
					for (DiscoveryNode node : event.nodesDelta().addedNodes()) {
						if (node.dataNode()) {
							routingTableDirty = true;
							break;
						}
					}
				}
			}
		} else {
			if (scheduledRoutingTableFuture != null) {
				scheduledRoutingTableFuture.cancel(true);
				scheduledRoutingTableFuture = null;
			}
		}
	}

	/**
	 * Reroute.
	 */
	private void reroute() {
		try {
			if (!routingTableDirty) {
				return;
			}
			if (lifecycle.stopped()) {
				return;
			}
			clusterService.submitStateUpdateTask(CLUSTER_UPDATE_TASK_SOURCE, new ClusterStateUpdateTask() {
				@Override
				public ClusterState execute(ClusterState currentState) {
					RoutingAllocation.Result routingResult = allocationService.reroute(currentState);
					if (!routingResult.changed()) {

						return currentState;
					}
					return ClusterState.newClusterStateBuilder().state(currentState).routingResult(routingResult)
							.build();
				}
			});
			routingTableDirty = false;
		} catch (Exception e) {
			logger.warn("Failed to reroute routing table", e);
		}
	}

	/**
	 * The Class RoutingTableUpdater.
	 *
	 * @author l.xue.nong
	 */
	private class RoutingTableUpdater implements Runnable {

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			reroute();
		}
	}
}
