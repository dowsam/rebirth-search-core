/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LocalDiscovery.java 2012-7-6 14:29:00 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery.local;

import static cn.com.rebirth.search.core.cluster.ClusterState.newClusterStateBuilder;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import jsr166y.LinkedTransferQueue;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ClusterState.Builder;
import cn.com.rebirth.search.core.cluster.ClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.ProcessedClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.block.ClusterBlocks;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodeService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.discovery.Discovery;
import cn.com.rebirth.search.core.discovery.InitialStateDiscoveryListener;
import cn.com.rebirth.search.core.node.service.NodeService;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class LocalDiscovery.
 *
 * @author l.xue.nong
 */
public class LocalDiscovery extends AbstractLifecycleComponent<Discovery> implements Discovery {

	/** The transport service. */
	private final TransportService transportService;

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The discovery node service. */
	private final DiscoveryNodeService discoveryNodeService;

	/** The cluster name. */
	private final ClusterName clusterName;

	/** The local node. */
	private DiscoveryNode localNode;

	/** The master. */
	private volatile boolean master = false;

	/** The initial state sent. */
	private final AtomicBoolean initialStateSent = new AtomicBoolean();

	/** The initial state listeners. */
	private final CopyOnWriteArrayList<InitialStateDiscoveryListener> initialStateListeners = new CopyOnWriteArrayList<InitialStateDiscoveryListener>();

	/** The Constant clusterGroups. */
	private static final ConcurrentMap<ClusterName, ClusterGroup> clusterGroups = new ConcurrentHashMap<ClusterName, ClusterGroup>();

	/** The Constant nodeIdGenerator. */
	private static final AtomicLong nodeIdGenerator = new AtomicLong();

	/**
	 * Instantiates a new local discovery.
	 *
	 * @param settings the settings
	 * @param clusterName the cluster name
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param discoveryNodeService the discovery node service
	 */
	@Inject
	public LocalDiscovery(Settings settings, ClusterName clusterName, TransportService transportService,
			ClusterService clusterService, DiscoveryNodeService discoveryNodeService) {
		super(settings);
		this.clusterName = clusterName;
		this.clusterService = clusterService;
		this.transportService = transportService;
		this.discoveryNodeService = discoveryNodeService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.discovery.Discovery#setNodeService(cn.com.rebirth.search.core.node.service.NodeService)
	 */
	@Override
	public void setNodeService(@Nullable NodeService nodeService) {

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
		synchronized (clusterGroups) {
			ClusterGroup clusterGroup = clusterGroups.get(clusterName);
			if (clusterGroup == null) {
				clusterGroup = new ClusterGroup();
				clusterGroups.put(clusterName, clusterGroup);
			}
			logger.debug("Connected to cluster [{}]", clusterName);
			this.localNode = new DiscoveryNode(settings.get("name"), Long.toString(nodeIdGenerator.incrementAndGet()),
					transportService.boundAddress().publishAddress(), discoveryNodeService.buildAttributes());

			clusterGroup.members().add(this);

			LocalDiscovery firstMaster = null;
			for (LocalDiscovery localDiscovery : clusterGroup.members()) {
				if (localDiscovery.localNode().masterNode()) {
					firstMaster = localDiscovery;
					break;
				}
			}

			if (firstMaster != null && firstMaster.equals(this)) {

				master = true;
				final LocalDiscovery master = firstMaster;
				clusterService.submitStateUpdateTask("local-disco-initial_connect(master)",
						new ProcessedClusterStateUpdateTask() {
							@Override
							public ClusterState execute(ClusterState currentState) {
								DiscoveryNodes.Builder nodesBuilder = DiscoveryNodes.newNodesBuilder();
								for (LocalDiscovery discovery : clusterGroups.get(clusterName).members()) {
									nodesBuilder.put(discovery.localNode);
								}
								nodesBuilder.localNodeId(master.localNode().id()).masterNodeId(master.localNode().id());

								ClusterBlocks.Builder blocks = ClusterBlocks.builder().blocks(currentState.blocks())
										.removeGlobalBlock(Discovery.NO_MASTER_BLOCK);
								return newClusterStateBuilder().state(currentState).nodes(nodesBuilder).blocks(blocks)
										.build();
							}

							@Override
							public void clusterStateProcessed(ClusterState clusterState) {
								sendInitialStateEventIfNeeded();
							}
						});
			} else if (firstMaster != null) {

				final ClusterState masterState = firstMaster.clusterService.state();
				clusterService.submitStateUpdateTask("local-disco(detected_master)", new ClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {

						DiscoveryNodes.Builder nodesBuilder = DiscoveryNodes.newNodesBuilder()
								.putAll(currentState.nodes()).put(localNode).localNodeId(localNode.id());
						return ClusterState.builder().state(currentState).metaData(masterState.metaData())
								.nodes(nodesBuilder).build();
					}
				});

				final LocalDiscovery master = firstMaster;
				firstMaster.clusterService.submitStateUpdateTask("local-disco-receive(from node[" + localNode + "])",
						new ProcessedClusterStateUpdateTask() {
							@Override
							public ClusterState execute(ClusterState currentState) {
								DiscoveryNodes.Builder nodesBuilder = DiscoveryNodes.newNodesBuilder();
								for (LocalDiscovery discovery : clusterGroups.get(clusterName).members()) {
									nodesBuilder.put(discovery.localNode);
								}
								nodesBuilder.localNodeId(master.localNode().id()).masterNodeId(master.localNode().id());
								return newClusterStateBuilder().state(currentState).nodes(nodesBuilder).build();
							}

							@Override
							public void clusterStateProcessed(ClusterState clusterState) {
								sendInitialStateEventIfNeeded();
							}
						});
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RebirthException {
		synchronized (clusterGroups) {
			ClusterGroup clusterGroup = clusterGroups.get(clusterName);
			if (clusterGroup == null) {
				logger.warn("Illegal state, should not have an empty cluster group when stopping, I should be there at teh very least...");
				return;
			}
			clusterGroup.members().remove(this);
			if (clusterGroup.members().isEmpty()) {

				clusterGroups.remove(clusterName);
				return;
			}

			LocalDiscovery firstMaster = null;
			for (LocalDiscovery localDiscovery : clusterGroup.members()) {
				if (localDiscovery.localNode().masterNode()) {
					firstMaster = localDiscovery;
					break;
				}
			}

			if (firstMaster != null) {

				if (master) {
					firstMaster.master = true;
				}

				final Set<String> newMembers = newHashSet();
				for (LocalDiscovery discovery : clusterGroup.members()) {
					newMembers.add(discovery.localNode.id());
				}

				final LocalDiscovery master = firstMaster;
				master.clusterService.submitStateUpdateTask("local-disco-update", new ClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {
						DiscoveryNodes newNodes = currentState.nodes().removeDeadMembers(newMembers,
								master.localNode.id());
						DiscoveryNodes.Delta delta = newNodes.delta(currentState.nodes());
						if (delta.added()) {
							logger.warn("No new nodes should be created when a new discovery view is accepted");
						}
						return newClusterStateBuilder().state(currentState).nodes(newNodes).build();
					}
				});
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.discovery.Discovery#localNode()
	 */
	@Override
	public DiscoveryNode localNode() {
		return localNode;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.discovery.Discovery#addListener(cn.com.rebirth.search.core.discovery.InitialStateDiscoveryListener)
	 */
	@Override
	public void addListener(InitialStateDiscoveryListener listener) {
		this.initialStateListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.discovery.Discovery#removeListener(cn.com.rebirth.search.core.discovery.InitialStateDiscoveryListener)
	 */
	@Override
	public void removeListener(InitialStateDiscoveryListener listener) {
		this.initialStateListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.discovery.Discovery#nodeDescription()
	 */
	@Override
	public String nodeDescription() {
		return clusterName.value() + "/" + localNode.id();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.discovery.Discovery#publish(cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	public void publish(ClusterState clusterState) {
		if (!master) {
			throw new RebirthIllegalStateException("Shouldn't publish state when not master");
		}
		ClusterGroup clusterGroup = clusterGroups.get(clusterName);
		if (clusterGroup == null) {

			return;
		}
		try {

			final byte[] clusterStateBytes = Builder.toBytes(clusterState);
			for (LocalDiscovery discovery : clusterGroup.members()) {
				if (discovery.master) {
					continue;
				}
				final ClusterState nodeSpecificClusterState = ClusterState.Builder.fromBytes(clusterStateBytes,
						discovery.localNode);

				if (nodeSpecificClusterState.nodes().localNode() != null) {
					discovery.clusterService.submitStateUpdateTask("local-disco-receive(from master)",
							new ProcessedClusterStateUpdateTask() {
								@Override
								public ClusterState execute(ClusterState currentState) {
									ClusterState.Builder builder = ClusterState.builder().state(
											nodeSpecificClusterState);

									if (nodeSpecificClusterState.routingTable().version() == currentState
											.routingTable().version()) {
										builder.routingTable(currentState.routingTable());
									}
									if (nodeSpecificClusterState.metaData().version() == currentState.metaData()
											.version()) {
										builder.metaData(currentState.metaData());
									}

									return builder.build();
								}

								@Override
								public void clusterStateProcessed(ClusterState clusterState) {
									sendInitialStateEventIfNeeded();
								}
							});
				}
			}
		} catch (Exception e) {

			throw new RebirthIllegalStateException("Cluster state failed to serialize", e);
		}
	}

	/**
	 * Send initial state event if needed.
	 */
	private void sendInitialStateEventIfNeeded() {
		if (initialStateSent.compareAndSet(false, true)) {
			for (InitialStateDiscoveryListener listener : initialStateListeners) {
				listener.initialStateProcessed();
			}
		}
	}

	/**
	 * The Class ClusterGroup.
	 *
	 * @author l.xue.nong
	 */
	private class ClusterGroup {

		/** The members. */
		private Queue<LocalDiscovery> members = new LinkedTransferQueue<LocalDiscovery>();

		/**
		 * Members.
		 *
		 * @return the queue
		 */
		Queue<LocalDiscovery> members() {
			return members;
		}
	}
}
