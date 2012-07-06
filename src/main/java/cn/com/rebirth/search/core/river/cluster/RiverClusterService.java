/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiverClusterService.java 2012-7-6 14:29:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river.cluster;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.concurrent.EsExecutors;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class RiverClusterService.
 *
 * @author l.xue.nong
 */
public class RiverClusterService extends AbstractLifecycleComponent<RiverClusterService> {

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The publish action. */
	private final PublishRiverClusterStateAction publishAction;

	/** The cluster state listeners. */
	private final List<RiverClusterStateListener> clusterStateListeners = new CopyOnWriteArrayList<RiverClusterStateListener>();

	/** The update tasks executor. */
	private volatile ExecutorService updateTasksExecutor;

	/** The cluster state. */
	private volatile RiverClusterState clusterState = RiverClusterState.builder().build();

	/**
	 * Instantiates a new river cluster service.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 */
	@Inject
	public RiverClusterService(Settings settings, TransportService transportService, ClusterService clusterService) {
		super(settings);
		this.clusterService = clusterService;

		this.publishAction = new PublishRiverClusterStateAction(settings, transportService, clusterService,
				new UpdateClusterStateListener());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
		this.updateTasksExecutor = newSingleThreadExecutor(EsExecutors.daemonThreadFactory(settings,
				"riverClusterService#updateTask"));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RebirthException {
		updateTasksExecutor.shutdown();
		try {
			updateTasksExecutor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {

		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
	}

	/**
	 * Adds the.
	 *
	 * @param listener the listener
	 */
	public void add(RiverClusterStateListener listener) {
		clusterStateListeners.add(listener);
	}

	/**
	 * Removes the.
	 *
	 * @param listener the listener
	 */
	public void remove(RiverClusterStateListener listener) {
		clusterStateListeners.remove(listener);
	}

	/**
	 * Submit state update task.
	 *
	 * @param source the source
	 * @param updateTask the update task
	 */
	public void submitStateUpdateTask(final String source, final RiverClusterStateUpdateTask updateTask) {
		if (!lifecycle.started()) {
			return;
		}
		updateTasksExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (!lifecycle.started()) {
					logger.debug("processing [{}]: ignoring, cluster_service not started", source);
					return;
				}
				logger.debug("processing [{}]: execute", source);

				RiverClusterState previousClusterState = clusterState;
				try {
					clusterState = updateTask.execute(previousClusterState);
				} catch (Exception e) {
					StringBuilder sb = new StringBuilder("failed to execute cluster state update, state:\nversion [")
							.append(clusterState.version()).append("], source [").append(source).append("]\n");
					logger.warn(sb.toString(), e);
					return;
				}
				if (previousClusterState != clusterState) {
					if (clusterService.state().nodes().localNodeMaster()) {

						clusterState = new RiverClusterState(clusterState.version() + 1, clusterState);
					} else {

						if (clusterState.version() < previousClusterState.version()) {
							logger.debug("got old cluster state [" + clusterState.version() + "<"
									+ previousClusterState.version() + "] from source [" + source + "], ignoring");
							return;
						}
					}

					if (logger.isTraceEnabled()) {
						StringBuilder sb = new StringBuilder("cluster state updated:\nversion [")
								.append(clusterState.version()).append("], source [").append(source).append("]\n");
						logger.trace(sb.toString());
					} else if (logger.isDebugEnabled()) {
						logger.debug("cluster state updated, version [{}], source [{}]", clusterState.version(), source);
					}

					RiverClusterChangedEvent clusterChangedEvent = new RiverClusterChangedEvent(source, clusterState,
							previousClusterState);

					for (RiverClusterStateListener listener : clusterStateListeners) {
						listener.riverClusterChanged(clusterChangedEvent);
					}

					if (clusterService.state().nodes().localNodeMaster()) {
						publishAction.publish(clusterState);
					}

					logger.debug("processing [{}]: done applying updated cluster_state", source);
				} else {
					logger.debug("processing [{}]: no change in cluster_state", source);
				}
			}
		});
	}

	/**
	 * The listener interface for receiving updateClusterState events.
	 * The class that is interested in processing a updateClusterState
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addUpdateClusterStateListener<code> method. When
	 * the updateClusterState event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see UpdateClusterStateEvent
	 */
	private class UpdateClusterStateListener implements PublishRiverClusterStateAction.NewClusterStateListener {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.river.cluster.PublishRiverClusterStateAction.NewClusterStateListener#onNewClusterState(cn.com.rebirth.search.core.river.cluster.RiverClusterState)
		 */
		@Override
		public void onNewClusterState(final RiverClusterState clusterState) {
			ClusterState state = clusterService.state();
			if (state.nodes().localNodeMaster()) {
				logger.warn("master should not receive new cluster state from [{}]", state.nodes().masterNode());
				return;
			}

			submitStateUpdateTask("received_state", new RiverClusterStateUpdateTask() {
				@Override
				public RiverClusterState execute(RiverClusterState currentState) {
					return clusterState;
				}
			});
		}
	}
}
