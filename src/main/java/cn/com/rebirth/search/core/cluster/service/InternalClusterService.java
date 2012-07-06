/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalClusterService.java 2012-3-29 15:02:46 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.service;

import static cn.com.rebirth.search.core.cluster.ClusterState.newClusterStateBuilder;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jsr166y.LinkedTransferQueue;
import cn.com.rebirth.commons.concurrent.EsExecutors;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.exception.RestartIllegalStateException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;
import cn.com.rebirth.search.core.cluster.ClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.ProcessedClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.TimeoutClusterStateListener;
import cn.com.rebirth.search.core.cluster.ClusterState.Builder;
import cn.com.rebirth.search.core.cluster.block.ClusterBlock;
import cn.com.rebirth.search.core.cluster.block.ClusterBlocks;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.cluster.routing.operation.OperationRouting;
import cn.com.rebirth.search.core.discovery.Discovery;
import cn.com.rebirth.search.core.discovery.DiscoveryService;
import cn.com.rebirth.search.core.node.settings.NodeSettingsService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;


/**
 * The Class InternalClusterService.
 *
 * @author l.xue.nong
 */
public class InternalClusterService extends AbstractLifecycleComponent<ClusterService> implements ClusterService {

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The discovery service. */
	private final DiscoveryService discoveryService;

	
	/** The operation routing. */
	private final OperationRouting operationRouting;

	
	/** The transport service. */
	private final TransportService transportService;

	
	/** The node settings service. */
	private final NodeSettingsService nodeSettingsService;

	
	/** The reconnect interval. */
	private final TimeValue reconnectInterval;

	
	/** The update tasks executor. */
	private volatile ExecutorService updateTasksExecutor;

	
	/** The priority cluster state listeners. */
	private final List<ClusterStateListener> priorityClusterStateListeners = new CopyOnWriteArrayList<ClusterStateListener>();

	
	/** The cluster state listeners. */
	private final List<ClusterStateListener> clusterStateListeners = new CopyOnWriteArrayList<ClusterStateListener>();

	
	/** The last cluster state listeners. */
	private final List<ClusterStateListener> lastClusterStateListeners = new CopyOnWriteArrayList<ClusterStateListener>();

	
	/** The on going timeouts. */
	private final Queue<NotifyTimeout> onGoingTimeouts = new LinkedTransferQueue<NotifyTimeout>();

	
	/** The cluster state. */
	private volatile ClusterState clusterState = newClusterStateBuilder().build();

	
	/** The initial blocks. */
	private final ClusterBlocks.Builder initialBlocks = ClusterBlocks.builder().addGlobalBlock(
			Discovery.NO_MASTER_BLOCK);

	
	/** The reconnect to nodes. */
	private volatile ScheduledFuture reconnectToNodes;

	
	/**
	 * Instantiates a new internal cluster service.
	 *
	 * @param settings the settings
	 * @param discoveryService the discovery service
	 * @param operationRouting the operation routing
	 * @param transportService the transport service
	 * @param nodeSettingsService the node settings service
	 * @param threadPool the thread pool
	 */
	@Inject
	public InternalClusterService(Settings settings, DiscoveryService discoveryService,
			OperationRouting operationRouting, TransportService transportService,
			NodeSettingsService nodeSettingsService, ThreadPool threadPool) {
		super(settings);
		this.operationRouting = operationRouting;
		this.transportService = transportService;
		this.discoveryService = discoveryService;
		this.threadPool = threadPool;
		this.nodeSettingsService = nodeSettingsService;

		this.nodeSettingsService.setClusterService(this);

		this.reconnectInterval = componentSettings.getAsTime("reconnect_interval", TimeValue.timeValueSeconds(10));
	}

	
	/**
	 * Settings service.
	 *
	 * @return the node settings service
	 */
	public NodeSettingsService settingsService() {
		return this.nodeSettingsService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterService#addInitialStateBlock(cn.com.summall.search.core.cluster.block.ClusterBlock)
	 */
	public void addInitialStateBlock(ClusterBlock block) throws RestartIllegalStateException {
		if (lifecycle.started()) {
			throw new RestartIllegalStateException("can't set initial block when started");
		}
		initialBlocks.addGlobalBlock(block);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RestartException {
		this.clusterState = newClusterStateBuilder().blocks(initialBlocks).build();
		this.updateTasksExecutor = newSingleThreadExecutor(EsExecutors.daemonThreadFactory(settings, "clusterService#updateTask"));
		this.reconnectToNodes = threadPool
				.schedule(reconnectInterval, ThreadPool.Names.GENERIC, new ReconnectToNodes());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RestartException {
		this.reconnectToNodes.cancel(true);
		for (NotifyTimeout onGoingTimeout : onGoingTimeouts) {
			onGoingTimeout.cancel();
			onGoingTimeout.listener.onClose();
		}
		updateTasksExecutor.shutdown();
		try {
			updateTasksExecutor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RestartException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterService#localNode()
	 */
	@Override
	public DiscoveryNode localNode() {
		return discoveryService.localNode();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterService#operationRouting()
	 */
	@Override
	public OperationRouting operationRouting() {
		return operationRouting;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterService#state()
	 */
	public ClusterState state() {
		return this.clusterState;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterService#addFirst(cn.com.summall.search.core.cluster.ClusterStateListener)
	 */
	public void addFirst(ClusterStateListener listener) {
		priorityClusterStateListeners.add(listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterService#addLast(cn.com.summall.search.core.cluster.ClusterStateListener)
	 */
	public void addLast(ClusterStateListener listener) {
		lastClusterStateListeners.add(listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterService#add(cn.com.summall.search.core.cluster.ClusterStateListener)
	 */
	public void add(ClusterStateListener listener) {
		clusterStateListeners.add(listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterService#remove(cn.com.summall.search.core.cluster.ClusterStateListener)
	 */
	public void remove(ClusterStateListener listener) {
		clusterStateListeners.remove(listener);
		priorityClusterStateListeners.remove(listener);
		lastClusterStateListeners.remove(listener);
		for (Iterator<NotifyTimeout> it = onGoingTimeouts.iterator(); it.hasNext();) {
			NotifyTimeout timeout = it.next();
			if (timeout.listener.equals(listener)) {
				timeout.cancel();
				it.remove();
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterService#add(cn.com.summall.search.commons.unit.TimeValue, cn.com.summall.search.core.cluster.TimeoutClusterStateListener)
	 */
	public void add(TimeValue timeout, final TimeoutClusterStateListener listener) {
		if (lifecycle.stoppedOrClosed()) {
			listener.onClose();
			return;
		}
		NotifyTimeout notifyTimeout = new NotifyTimeout(listener, timeout);
		notifyTimeout.future = threadPool.schedule(timeout, ThreadPool.Names.GENERIC, notifyTimeout);
		onGoingTimeouts.add(notifyTimeout);
		clusterStateListeners.add(listener);
		
		updateTasksExecutor.execute(new Runnable() {
			@Override
			public void run() {
				listener.postAdded();
			}
		});
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterService#submitStateUpdateTask(java.lang.String, cn.com.summall.search.core.cluster.ClusterStateUpdateTask)
	 */
	public void submitStateUpdateTask(final String source, final ClusterStateUpdateTask updateTask) {
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
				ClusterState previousClusterState = clusterState;
				ClusterState newClusterState;
				try {
					newClusterState = updateTask.execute(previousClusterState);
				} catch (Exception e) {
					StringBuilder sb = new StringBuilder("failed to execute cluster state update, state:\nversion [")
							.append(previousClusterState.version()).append("], source [").append(source).append("]\n");
					sb.append(previousClusterState.nodes().prettyPrint());
					sb.append(previousClusterState.routingTable().prettyPrint());
					sb.append(previousClusterState.readOnlyRoutingNodes().prettyPrint());
					logger.warn(sb.toString(), e);
					return;
				}

				if (previousClusterState == newClusterState) {
					logger.debug("processing [{}]: no change in cluster_state", source);
					return;
				}

				try {
					if (newClusterState.nodes().localNodeMaster()) {
						
						Builder builder = ClusterState.builder().state(newClusterState)
								.version(newClusterState.version() + 1);
						if (previousClusterState.routingTable() != newClusterState.routingTable()) {
							builder.routingTable(RoutingTable.builder().routingTable(newClusterState.routingTable())
									.version(newClusterState.routingTable().version() + 1));
						}
						if (previousClusterState.metaData() != newClusterState.metaData()) {
							builder.metaData(MetaData.builder().metaData(newClusterState.metaData())
									.version(newClusterState.metaData().version() + 1));
						}
						newClusterState = builder.build();
					} else {
						if (previousClusterState.blocks().hasGlobalBlock(Discovery.NO_MASTER_BLOCK)
								&& !newClusterState.blocks().hasGlobalBlock(Discovery.NO_MASTER_BLOCK)) {
							
							
							Builder builder = ClusterState.builder().state(newClusterState);
							builder.routingTable(RoutingTable.builder().routingTable(newClusterState.routingTable()));
							builder.metaData(MetaData.builder().metaData(newClusterState.metaData()));
							newClusterState = builder.build();
							logger.debug("got first state from fresh master [{}]", newClusterState.nodes()
									.masterNodeId());
						} else if (newClusterState.version() < previousClusterState.version()) {
							
							logger.debug("got old cluster state [" + newClusterState.version() + "<"
									+ previousClusterState.version() + "] from source [" + source + "], ignoring");
							return;
						}
					}

					if (logger.isTraceEnabled()) {
						StringBuilder sb = new StringBuilder("cluster state updated:\nversion [")
								.append(newClusterState.version()).append("], source [").append(source).append("]\n");
						sb.append(newClusterState.nodes().prettyPrint());
						sb.append(newClusterState.routingTable().prettyPrint());
						sb.append(newClusterState.readOnlyRoutingNodes().prettyPrint());
						logger.trace(sb.toString());
					} else if (logger.isDebugEnabled()) {
						logger.debug("cluster state updated, version [{}], source [{}]", newClusterState.version(),
								source);
					}

					ClusterChangedEvent clusterChangedEvent = new ClusterChangedEvent(source, newClusterState,
							previousClusterState);
					
					final DiscoveryNodes.Delta nodesDelta = clusterChangedEvent.nodesDelta();
					if (nodesDelta.hasChanges() && logger.isInfoEnabled()) {
						String summary = nodesDelta.shortSummary();
						if (summary.length() > 0) {
							logger.info("{}, reason: {}", summary, source);
						}
					}

					
					for (DiscoveryNode node : nodesDelta.addedNodes()) {
						if (!nodeRequiresConnection(node)) {
							continue;
						}
						try {
							transportService.connectToNode(node);
						} catch (Exception e) {
							
							logger.warn("failed to connect to node [" + node + "]", e);
						}
					}

					
					
					
					if (newClusterState.nodes().localNodeMaster()) {
						discoveryService.publish(newClusterState);
					}

					
					clusterState = newClusterState;

					for (ClusterStateListener listener : priorityClusterStateListeners) {
						listener.clusterChanged(clusterChangedEvent);
					}
					for (ClusterStateListener listener : clusterStateListeners) {
						listener.clusterChanged(clusterChangedEvent);
					}
					for (ClusterStateListener listener : lastClusterStateListeners) {
						listener.clusterChanged(clusterChangedEvent);
					}

					if (!nodesDelta.removedNodes().isEmpty()) {
						threadPool.generic().execute(new Runnable() {
							@Override
							public void run() {
								for (DiscoveryNode node : nodesDelta.removedNodes()) {
									transportService.disconnectFromNode(node);
								}
							}
						});
					}

					if (updateTask instanceof ProcessedClusterStateUpdateTask) {
						((ProcessedClusterStateUpdateTask) updateTask).clusterStateProcessed(newClusterState);
					}

					logger.debug("processing [{}]: done applying updated cluster_state", source);
				} catch (Exception e) {
					StringBuilder sb = new StringBuilder("failed to apply updated cluster state:\nversion [")
							.append(newClusterState.version()).append("], source [").append(source).append("]\n");
					sb.append(newClusterState.nodes().prettyPrint());
					sb.append(newClusterState.routingTable().prettyPrint());
					sb.append(newClusterState.readOnlyRoutingNodes().prettyPrint());
					logger.warn(sb.toString(), e);
				}
			}
		});
	}

	
	/**
	 * The Class NotifyTimeout.
	 *
	 * @author l.xue.nong
	 */
	class NotifyTimeout implements Runnable {

		
		/** The listener. */
		final TimeoutClusterStateListener listener;

		
		/** The timeout. */
		final TimeValue timeout;

		
		/** The future. */
		ScheduledFuture future;

		
		/**
		 * Instantiates a new notify timeout.
		 *
		 * @param listener the listener
		 * @param timeout the timeout
		 */
		NotifyTimeout(TimeoutClusterStateListener listener, TimeValue timeout) {
			this.listener = listener;
			this.timeout = timeout;
		}

		
		/**
		 * Cancel.
		 */
		public void cancel() {
			future.cancel(false);
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (future.isCancelled()) {
				return;
			}
			if (lifecycle.stoppedOrClosed()) {
				listener.onClose();
			} else {
				listener.onTimeout(this.timeout);
			}
			
		}
	}

	
	/**
	 * The Class ReconnectToNodes.
	 *
	 * @author l.xue.nong
	 */
	private class ReconnectToNodes implements Runnable {

		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			
			
			for (DiscoveryNode node : clusterState.nodes()) {
				if (lifecycle.stoppedOrClosed()) {
					return;
				}
				if (!nodeRequiresConnection(node)) {
					continue;
				}
				if (clusterState.nodes().nodeExists(node.id())) { 
					if (!transportService.nodeConnected(node)) {
						try {
							transportService.connectToNode(node);
						} catch (Exception e) {
							if (lifecycle.stoppedOrClosed()) {
								return;
							}
							if (clusterState.nodes().nodeExists(node.id())) { 
								logger.warn("failed to reconnect to node {}", e, node);
							}
						}
					}
				}
			}
			if (lifecycle.started()) {
				reconnectToNodes = threadPool.schedule(reconnectInterval, ThreadPool.Names.GENERIC, this);
			}
		}
	}

	
	/**
	 * Node requires connection.
	 *
	 * @param node the node
	 * @return true, if successful
	 */
	private boolean nodeRequiresConnection(DiscoveryNode node) {
		return localNode().shouldConnectTo(node);
	}
}