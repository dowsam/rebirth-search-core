/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ZenDiscovery.java 2012-3-29 15:02:38 l.xue.nong$$
 */


package cn.com.rebirth.search.core.discovery.zen;

import static cn.com.rebirth.search.core.cluster.ClusterState.newClusterStateBuilder;
import static cn.com.rebirth.search.core.cluster.node.DiscoveryNodes.newNodesBuilder;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.exception.RestartIllegalStateException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.UUID;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.component.Lifecycle;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.ProcessedClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.block.ClusterBlocks;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodeService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.discovery.Discovery;
import cn.com.rebirth.search.core.discovery.InitialStateDiscoveryListener;
import cn.com.rebirth.search.core.discovery.zen.elect.ElectMasterService;
import cn.com.rebirth.search.core.discovery.zen.fd.MasterFaultDetection;
import cn.com.rebirth.search.core.discovery.zen.fd.NodesFaultDetection;
import cn.com.rebirth.search.core.discovery.zen.membership.MembershipAction;
import cn.com.rebirth.search.core.discovery.zen.ping.ZenPing;
import cn.com.rebirth.search.core.discovery.zen.ping.ZenPingService;
import cn.com.rebirth.search.core.discovery.zen.publish.PublishClusterStateAction;
import cn.com.rebirth.search.core.gateway.GatewayService;
import cn.com.rebirth.search.core.node.service.NodeService;
import cn.com.rebirth.search.core.node.settings.NodeSettingsService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.Sets;


/**
 * The Class ZenDiscovery.
 *
 * @author l.xue.nong
 */
public class ZenDiscovery extends AbstractLifecycleComponent<Discovery> implements Discovery, DiscoveryNodesProvider {

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The transport service. */
	private final TransportService transportService;

	
	/** The cluster service. */
	private final ClusterService clusterService;

	
	/** The cluster name. */
	private final ClusterName clusterName;

	
	/** The discovery node service. */
	private final DiscoveryNodeService discoveryNodeService;

	
	/** The ping service. */
	private final ZenPingService pingService;

	
	/** The master fd. */
	private final MasterFaultDetection masterFD;

	
	/** The nodes fd. */
	private final NodesFaultDetection nodesFD;

	
	/** The publish cluster state. */
	private final PublishClusterStateAction publishClusterState;

	
	/** The membership. */
	private final MembershipAction membership;

	
	/** The ping timeout. */
	private final TimeValue pingTimeout;

	
	
	/** The send leave request. */
	private final boolean sendLeaveRequest;

	
	/** The elect master. */
	private final ElectMasterService electMaster;

	
	/** The local node. */
	private DiscoveryNode localNode;

	
	/** The initial state listeners. */
	private final CopyOnWriteArrayList<InitialStateDiscoveryListener> initialStateListeners = new CopyOnWriteArrayList<InitialStateDiscoveryListener>();

	
	/** The master. */
	private volatile boolean master = false;

	
	/** The latest disco nodes. */
	private volatile DiscoveryNodes latestDiscoNodes;

	
	/** The current join thread. */
	private volatile Thread currentJoinThread;

	
	/** The initial state sent. */
	private final AtomicBoolean initialStateSent = new AtomicBoolean();

	
	/** The node service. */
	@Nullable
	private NodeService nodeService;

	
	/**
	 * Instantiates a new zen discovery.
	 *
	 * @param settings the settings
	 * @param clusterName the cluster name
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param nodeSettingsService the node settings service
	 * @param discoveryNodeService the discovery node service
	 * @param pingService the ping service
	 */
	@Inject
	public ZenDiscovery(Settings settings, ClusterName clusterName, ThreadPool threadPool,
			TransportService transportService, ClusterService clusterService, NodeSettingsService nodeSettingsService,
			DiscoveryNodeService discoveryNodeService, ZenPingService pingService) {
		super(settings);
		this.clusterName = clusterName;
		this.threadPool = threadPool;
		this.clusterService = clusterService;
		this.transportService = transportService;
		this.discoveryNodeService = discoveryNodeService;
		this.pingService = pingService;

		
		this.pingTimeout = settings.getAsTime(
				"discovery.zen.ping.timeout",
				settings.getAsTime(
						"discovery.zen.ping_timeout",
						componentSettings.getAsTime("ping_timeout",
								componentSettings.getAsTime("initial_ping_timeout", TimeValue.timeValueSeconds(3)))));
		this.sendLeaveRequest = componentSettings.getAsBoolean("send_leave_request", true);

		logger.debug("using ping.timeout [{}]", pingTimeout);

		this.electMaster = new ElectMasterService(settings, nodeSettingsService);

		this.masterFD = new MasterFaultDetection(settings, threadPool, transportService, this);
		this.masterFD.addListener(new MasterNodeFailureListener());

		this.nodesFD = new NodesFaultDetection(settings, threadPool, transportService);
		this.nodesFD.addListener(new NodeFailureListener());

		this.publishClusterState = new PublishClusterStateAction(settings, transportService, this,
				new NewClusterStateListener());
		this.pingService.setNodesProvider(this);
		this.membership = new MembershipAction(settings, transportService, this, new MembershipListener());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.discovery.Discovery#setNodeService(cn.com.summall.search.core.node.service.NodeService)
	 */
	@Override
	public void setNodeService(@Nullable NodeService nodeService) {
		this.nodeService = nodeService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RestartException {
		Map<String, String> nodeAttributes = discoveryNodeService.buildAttributes();
		
		String nodeId = UUID.randomBase64UUID();
		localNode = new DiscoveryNode(settings.get("name"), nodeId, transportService.boundAddress().publishAddress(),
				nodeAttributes);
		latestDiscoNodes = new DiscoveryNodes.Builder().put(localNode).localNodeId(localNode.id()).build();
		nodesFD.updateNodes(latestDiscoNodes);
		pingService.start();

		
		asyncJoinCluster();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RestartException {
		pingService.stop();
		masterFD.stop("zen disco stop");
		nodesFD.stop();
		initialStateSent.set(false);
		if (sendLeaveRequest) {
			if (!master && latestDiscoNodes.masterNode() != null) {
				try {
					membership.sendLeaveRequestBlocking(latestDiscoNodes.masterNode(), localNode,
							TimeValue.timeValueSeconds(1));
				} catch (Exception e) {
					logger.debug("failed to send leave request to master [{}]", e, latestDiscoNodes.masterNode());
				}
			} else {
				DiscoveryNode[] possibleMasters = electMaster.nextPossibleMasters(latestDiscoNodes.nodes().values(), 5);
				for (DiscoveryNode possibleMaster : possibleMasters) {
					if (localNode.equals(possibleMaster)) {
						continue;
					}
					try {
						membership.sendLeaveRequest(latestDiscoNodes.masterNode(), possibleMaster);
					} catch (Exception e) {
						logger.debug("failed to send leave request from master [" + latestDiscoNodes.masterNode()
								+ "] to possible master [" + possibleMaster + "]", e);
					}
				}
			}
		}
		master = false;
		if (currentJoinThread != null) {
			try {
				currentJoinThread.interrupt();
			} catch (Exception e) {
				
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RestartException {
		masterFD.close();
		nodesFD.close();
		publishClusterState.close();
		membership.close();
		pingService.close();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.discovery.Discovery#localNode()
	 */
	@Override
	public DiscoveryNode localNode() {
		return localNode;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.discovery.Discovery#addListener(cn.com.summall.search.core.discovery.InitialStateDiscoveryListener)
	 */
	@Override
	public void addListener(InitialStateDiscoveryListener listener) {
		this.initialStateListeners.add(listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.discovery.Discovery#removeListener(cn.com.summall.search.core.discovery.InitialStateDiscoveryListener)
	 */
	@Override
	public void removeListener(InitialStateDiscoveryListener listener) {
		this.initialStateListeners.remove(listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.discovery.Discovery#nodeDescription()
	 */
	@Override
	public String nodeDescription() {
		return clusterName.value() + "/" + localNode.id();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.discovery.zen.DiscoveryNodesProvider#nodes()
	 */
	@Override
	public DiscoveryNodes nodes() {
		DiscoveryNodes latestNodes = this.latestDiscoNodes;
		if (latestNodes != null) {
			return latestNodes;
		}
		
		return newNodesBuilder().put(localNode).localNodeId(localNode.id()).build();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.discovery.zen.DiscoveryNodesProvider#nodeService()
	 */
	@Override
	public NodeService nodeService() {
		return this.nodeService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.discovery.Discovery#publish(cn.com.summall.search.core.cluster.ClusterState)
	 */
	@Override
	public void publish(ClusterState clusterState) {
		if (!master) {
			throw new RestartIllegalStateException("Shouldn't publish state when not master");
		}
		latestDiscoNodes = clusterState.nodes();
		nodesFD.updateNodes(clusterState.nodes());
		publishClusterState.publish(clusterState);
	}

	
	/**
	 * Async join cluster.
	 */
	private void asyncJoinCluster() {
		if (currentJoinThread != null) {
			
			return;
		}
		threadPool.generic().execute(new Runnable() {
			@Override
			public void run() {
				currentJoinThread = Thread.currentThread();
				try {
					innterJoinCluster();
				} finally {
					currentJoinThread = null;
				}
			}
		});
	}

	
	/**
	 * Innter join cluster.
	 */
	private void innterJoinCluster() {
		boolean retry = true;
		while (retry) {
			if (lifecycle.stoppedOrClosed()) {
				return;
			}
			retry = false;
			DiscoveryNode masterNode = findMaster();
			if (masterNode == null) {
				retry = true;
				continue;
			}
			if (localNode.equals(masterNode)) {
				this.master = true;
				nodesFD.start(); 
				clusterService.submitStateUpdateTask("zen-disco-join (elected_as_master)",
						new ProcessedClusterStateUpdateTask() {
							@Override
							public ClusterState execute(ClusterState currentState) {
								DiscoveryNodes.Builder builder = new DiscoveryNodes.Builder()
										.localNodeId(localNode.id()).masterNodeId(localNode.id())
										
										.put(localNode);
								
								latestDiscoNodes = builder.build();
								ClusterBlocks clusterBlocks = ClusterBlocks.builder().blocks(currentState.blocks())
										.removeGlobalBlock(NO_MASTER_BLOCK).build();
								return newClusterStateBuilder().state(currentState).nodes(builder)
										.blocks(clusterBlocks).build();
							}

							@Override
							public void clusterStateProcessed(ClusterState clusterState) {
								sendInitialStateEventIfNeeded();
							}
						});
			} else {
				this.master = false;
				try {
					
					transportService.connectToNode(masterNode);
				} catch (Exception e) {
					logger.warn("failed to connect to master [{}], retrying...", e, masterNode);
					retry = true;
					continue;
				}
				
				ClusterState joinClusterStateX;
				try {
					joinClusterStateX = membership.sendJoinRequestBlocking(masterNode, localNode, pingTimeout);
				} catch (Exception e) {
					if (e instanceof RestartException) {
						logger.info("failed to send join request to master [{}], reason [{}]", masterNode,
								((RestartException) e).getDetailedMessage());
					} else {
						logger.info("failed to send join request to master [{}], reason [{}]", masterNode,
								e.getMessage());
					}
					if (logger.isTraceEnabled()) {
						logger.trace("detailed failed reason", e);
					}
					
					retry = true;
					continue;
				}
				masterFD.start(masterNode, "initial_join");
				
				
			}
		}
	}

	
	/**
	 * Handle leave request.
	 *
	 * @param node the node
	 */
	private void handleLeaveRequest(final DiscoveryNode node) {
		if (lifecycleState() != Lifecycle.State.STARTED) {
			
			return;
		}
		if (master) {
			clusterService.submitStateUpdateTask("zen-disco-node_left(" + node + ")", new ClusterStateUpdateTask() {
				@Override
				public ClusterState execute(ClusterState currentState) {
					DiscoveryNodes.Builder builder = new DiscoveryNodes.Builder().putAll(currentState.nodes()).remove(
							node.id());
					latestDiscoNodes = builder.build();
					currentState = newClusterStateBuilder().state(currentState).nodes(latestDiscoNodes).build();
					
					if (!electMaster.hasEnoughMasterNodes(currentState.nodes())) {
						return disconnectFromCluster(currentState, "not enough master nodes");
					}
					return currentState;
				}
			});
		} else {
			handleMasterGone(node, "shut_down");
		}
	}

	
	/**
	 * Handle node failure.
	 *
	 * @param node the node
	 * @param reason the reason
	 */
	private void handleNodeFailure(final DiscoveryNode node, String reason) {
		if (lifecycleState() != Lifecycle.State.STARTED) {
			
			return;
		}
		if (!master) {
			
			return;
		}
		clusterService.submitStateUpdateTask("zen-disco-node_failed(" + node + "), reason " + reason,
				new ProcessedClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {
						DiscoveryNodes.Builder builder = new DiscoveryNodes.Builder().putAll(currentState.nodes())
								.remove(node.id());
						latestDiscoNodes = builder.build();
						currentState = newClusterStateBuilder().state(currentState).nodes(latestDiscoNodes).build();
						
						if (!electMaster.hasEnoughMasterNodes(currentState.nodes())) {
							return disconnectFromCluster(currentState, "not enough master nodes");
						}
						return currentState;
					}

					@Override
					public void clusterStateProcessed(ClusterState clusterState) {
						sendInitialStateEventIfNeeded();
					}
				});
	}

	
	/**
	 * Handle master gone.
	 *
	 * @param masterNode the master node
	 * @param reason the reason
	 */
	private void handleMasterGone(final DiscoveryNode masterNode, final String reason) {
		if (lifecycleState() != Lifecycle.State.STARTED) {
			
			return;
		}
		if (master) {
			
			return;
		}

		logger.info("master_left [{}], reason [{}]", masterNode, reason);

		clusterService.submitStateUpdateTask("zen-disco-master_failed (" + masterNode + ")",
				new ProcessedClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {
						if (!masterNode.id().equals(currentState.nodes().masterNodeId())) {
							
							return currentState;
						}

						DiscoveryNodes.Builder nodesBuilder = DiscoveryNodes.newNodesBuilder()
								.putAll(currentState.nodes())
								
								.remove(masterNode.id()).masterNodeId(null);

						if (!electMaster.hasEnoughMasterNodes(nodesBuilder.build())) {
							return disconnectFromCluster(ClusterState.builder().state(currentState).nodes(nodesBuilder)
									.build(), "not enough master nodes after master left (reason = " + reason + ")");
						}

						final DiscoveryNode electedMaster = electMaster.electMaster(nodesBuilder.build()); 
						if (localNode.equals(electedMaster)) {
							master = true;
							masterFD.stop("got elected as new master since master left (reason = " + reason + ")");
							nodesFD.start();
							nodesBuilder.masterNodeId(localNode.id());
							latestDiscoNodes = nodesBuilder.build();
							return newClusterStateBuilder().state(currentState).nodes(latestDiscoNodes).build();
						} else {
							nodesFD.stop();
							if (electedMaster != null) {
								nodesBuilder.masterNodeId(electedMaster.id());
								masterFD.restart(electedMaster, "possible elected master since master left (reason = "
										+ reason + ")");
								latestDiscoNodes = nodesBuilder.build();
								return newClusterStateBuilder().state(currentState).nodes(latestDiscoNodes).build();
							} else {
								return disconnectFromCluster(
										newClusterStateBuilder().state(currentState).nodes(nodesBuilder.build())
												.build(), "master_left and no other node elected to become master");
							}
						}
					}

					@Override
					public void clusterStateProcessed(ClusterState clusterState) {
						sendInitialStateEventIfNeeded();
					}

				});
	}

	
	/**
	 * Handle new cluster state from master.
	 *
	 * @param newState the new state
	 */
	void handleNewClusterStateFromMaster(final ClusterState newState) {
		if (master) {
			logger.warn("master should not receive new cluster state from [{}]", newState.nodes().masterNode());
		} else {
			if (newState.nodes().localNode() == null) {
				logger.warn("received a cluster state from [{}] and not part of the cluster, should not happen",
						newState.nodes().masterNode());
			} else {
				if (currentJoinThread != null) {
					logger.debug("got a new state from master node, though we are already trying to rejoin the cluster");
				}

				clusterService.submitStateUpdateTask("zen-disco-receive(from master [" + newState.nodes().masterNode()
						+ "])", new ProcessedClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {

						latestDiscoNodes = newState.nodes();

						
						if (masterFD.masterNode() == null
								|| !masterFD.masterNode().equals(latestDiscoNodes.masterNode())) {
							masterFD.restart(
									latestDiscoNodes.masterNode(),
									"new cluster stare received and we monitor the wrong master ["
											+ masterFD.masterNode() + "]");
						}

						ClusterState.Builder builder = ClusterState.builder().state(newState);
						
						if (newState.routingTable().version() == currentState.routingTable().version()) {
							builder.routingTable(currentState.routingTable());
						}
						
						if (newState.metaData().version() == currentState.metaData().version()) {
							builder.metaData(currentState.metaData());
						} else {
							
							MetaData.Builder metaDataBuilder = MetaData.builder().metaData(newState.metaData())
									.removeAllIndices();
							for (IndexMetaData indexMetaData : newState.metaData()) {
								IndexMetaData currentIndexMetaData = currentState.metaData().index(
										indexMetaData.index());
								if (currentIndexMetaData == null
										|| currentIndexMetaData.version() != indexMetaData.version()) {
									metaDataBuilder.put(indexMetaData, false);
								} else {
									metaDataBuilder.put(currentIndexMetaData, false);
								}
							}
							builder.metaData(metaDataBuilder);
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
	}

	
	/**
	 * Handle join request.
	 *
	 * @param node the node
	 * @return the cluster state
	 */
	private ClusterState handleJoinRequest(final DiscoveryNode node) {
		if (!master) {
			throw new RestartIllegalStateException("Node [" + localNode + "] not master for join request from ["
					+ node + "]");
		}

		ClusterState state = clusterService.state();
		if (!transportService.addressSupported(node.address().getClass())) {
			
			logger.warn("received a wrong address type from [{}], ignoring...", node);
		} else {
			transportService.connectToNode(node);
			state = clusterService.state();

			clusterService.submitStateUpdateTask("zen-disco-receive(join from node[" + node + "])",
					new ClusterStateUpdateTask() {
						@Override
						public ClusterState execute(ClusterState currentState) {
							if (currentState.nodes().nodeExists(node.id())) {
								
								logger.warn("received a join request for an existing node [{}]", node);
								
								return ClusterState.builder().state(currentState).build();
							}
							return newClusterStateBuilder().state(currentState)
									.nodes(currentState.nodes().newNode(node)).build();
						}
					});
		}
		return state;
	}

	
	/**
	 * Find master.
	 *
	 * @return the discovery node
	 */
	private DiscoveryNode findMaster() {
		ZenPing.PingResponse[] pingResponses = pingService.pingAndWait(pingTimeout);
		if (pingResponses == null) {
			return null;
		}
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("ping responses:");
			if (pingResponses.length == 0) {
				sb.append(" {none}");
			} else {
				for (ZenPing.PingResponse pingResponse : pingResponses) {
					sb.append("\n\t--> ").append("target [").append(pingResponse.target()).append("], master [")
							.append(pingResponse.master()).append("]");
				}
			}
			logger.debug(sb.toString());
		}
		List<DiscoveryNode> pingMasters = newArrayList();
		for (ZenPing.PingResponse pingResponse : pingResponses) {
			if (pingResponse.master() != null) {
				pingMasters.add(pingResponse.master());
			}
		}

		Set<DiscoveryNode> possibleMasterNodes = Sets.newHashSet();
		possibleMasterNodes.add(localNode);
		for (ZenPing.PingResponse pingResponse : pingResponses) {
			possibleMasterNodes.add(pingResponse.target());
		}
		
		
		if (!electMaster.hasEnoughMasterNodes(possibleMasterNodes)) {
			return null;
		}

		if (pingMasters.isEmpty()) {
			
			DiscoveryNode electedMaster = electMaster.electMaster(possibleMasterNodes);
			if (localNode.equals(electedMaster)) {
				return localNode;
			}
		} else {
			DiscoveryNode electedMaster = electMaster.electMaster(pingMasters);
			if (electedMaster != null) {
				return electedMaster;
			}
		}
		return null;
	}

	
	/**
	 * Disconnect from cluster.
	 *
	 * @param clusterState the cluster state
	 * @param reason the reason
	 * @return the cluster state
	 */
	private ClusterState disconnectFromCluster(ClusterState clusterState, String reason) {
		logger.warn(reason + ", current nodes: {}", clusterState.nodes());
		nodesFD.stop();
		masterFD.stop(reason);
		master = false;

		ClusterBlocks clusterBlocks = ClusterBlocks.builder().blocks(clusterState.blocks())
				.addGlobalBlock(NO_MASTER_BLOCK).addGlobalBlock(GatewayService.STATE_NOT_RECOVERED_BLOCK).build();

		
		RoutingTable routingTable = RoutingTable.builder().version(clusterState.routingTable().version()).build();
		
		MetaData metaData = MetaData.builder().build();

		
		latestDiscoNodes = new DiscoveryNodes.Builder().put(localNode).localNodeId(localNode.id()).build();

		asyncJoinCluster();

		return newClusterStateBuilder().state(clusterState).blocks(clusterBlocks).nodes(latestDiscoNodes)
				.routingTable(routingTable).metaData(metaData).build();
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
	 * The listener interface for receiving newClusterState events.
	 * The class that is interested in processing a newClusterState
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addNewClusterStateListener<code> method. When
	 * the newClusterState event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see NewClusterStateEvent
	 */
	private class NewClusterStateListener implements PublishClusterStateAction.NewClusterStateListener {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.discovery.zen.publish.PublishClusterStateAction.NewClusterStateListener#onNewClusterState(cn.com.summall.search.core.cluster.ClusterState)
		 */
		@Override
		public void onNewClusterState(ClusterState clusterState) {
			handleNewClusterStateFromMaster(clusterState);
		}
	}

	
	/**
	 * The listener interface for receiving membership events.
	 * The class that is interested in processing a membership
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addMembershipListener<code> method. When
	 * the membership event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see MembershipEvent
	 */
	private class MembershipListener implements MembershipAction.MembershipListener {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.discovery.zen.membership.MembershipAction.MembershipListener#onJoin(cn.com.summall.search.core.cluster.node.DiscoveryNode)
		 */
		@Override
		public ClusterState onJoin(DiscoveryNode node) {
			return handleJoinRequest(node);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.discovery.zen.membership.MembershipAction.MembershipListener#onLeave(cn.com.summall.search.core.cluster.node.DiscoveryNode)
		 */
		@Override
		public void onLeave(DiscoveryNode node) {
			handleLeaveRequest(node);
		}
	}

	
	/**
	 * The listener interface for receiving nodeFailure events.
	 * The class that is interested in processing a nodeFailure
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addNodeFailureListener<code> method. When
	 * the nodeFailure event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see NodeFailureEvent
	 */
	private class NodeFailureListener implements NodesFaultDetection.Listener {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.discovery.zen.fd.NodesFaultDetection.Listener#onNodeFailure(cn.com.summall.search.core.cluster.node.DiscoveryNode, java.lang.String)
		 */
		@Override
		public void onNodeFailure(DiscoveryNode node, String reason) {
			handleNodeFailure(node, reason);
		}
	}

	
	/**
	 * The listener interface for receiving masterNodeFailure events.
	 * The class that is interested in processing a masterNodeFailure
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addMasterNodeFailureListener<code> method. When
	 * the masterNodeFailure event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see MasterNodeFailureEvent
	 */
	private class MasterNodeFailureListener implements MasterFaultDetection.Listener {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.discovery.zen.fd.MasterFaultDetection.Listener#onMasterFailure(cn.com.summall.search.core.cluster.node.DiscoveryNode, java.lang.String)
		 */
		@Override
		public void onMasterFailure(DiscoveryNode masterNode, String reason) {
			handleMasterGone(masterNode, reason);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.discovery.zen.fd.MasterFaultDetection.Listener#onDisconnectedFromMaster()
		 */
		@Override
		public void onDisconnectedFromMaster() {
			
			DiscoveryNode masterNode = latestDiscoNodes.masterNode();
			try {
				membership.sendJoinRequest(masterNode, localNode);
			} catch (Exception e) {
				logger.warn("failed to send join request on disconnection from master [{}]", masterNode);
			}
		}
	}
}
