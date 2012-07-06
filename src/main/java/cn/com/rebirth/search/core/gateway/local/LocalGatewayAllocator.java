/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core LocalGatewayAllocator.java 2012-3-29 15:01:58 l.xue.nong$$
 */


package cn.com.rebirth.search.core.gateway.local;

import gnu.trove.iterator.TObjectLongIterator;
import gnu.trove.map.hash.TObjectLongHashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.RoutingNodes;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.FailedRerouteAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.StartedRerouteAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.allocator.GatewayAllocator;
import cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecider;
import cn.com.rebirth.search.core.gateway.local.state.shards.TransportNodesListGatewayStartedShards;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.store.StoreFileMetaData;
import cn.com.rebirth.search.core.indices.store.TransportNodesListShardStoreMetaData;
import cn.com.rebirth.search.core.transport.ConnectTransportException;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


/**
 * The Class LocalGatewayAllocator.
 *
 * @author l.xue.nong
 */
public class LocalGatewayAllocator extends AbstractComponent implements GatewayAllocator {

	static {
		IndexMetaData.addDynamicSettings("index.recovery.initial_shards");
	}

	
	/** The list gateway started shards. */
	private final TransportNodesListGatewayStartedShards listGatewayStartedShards;

	
	/** The list shard store meta data. */
	private final TransportNodesListShardStoreMetaData listShardStoreMetaData;

	
	/** The cached stores. */
	private final ConcurrentMap<ShardId, Map<DiscoveryNode, TransportNodesListShardStoreMetaData.StoreFilesMetaData>> cachedStores = ConcurrentCollections
			.newConcurrentMap();

	
	/** The cached shards state. */
	private final ConcurrentMap<ShardId, TObjectLongHashMap<DiscoveryNode>> cachedShardsState = ConcurrentCollections
			.newConcurrentMap();

	
	/** The list timeout. */
	private final TimeValue listTimeout;

	
	/** The initial shards. */
	private final String initialShards;

	
	/**
	 * Instantiates a new local gateway allocator.
	 *
	 * @param settings the settings
	 * @param listGatewayStartedShards the list gateway started shards
	 * @param listShardStoreMetaData the list shard store meta data
	 */
	@Inject
	public LocalGatewayAllocator(Settings settings, TransportNodesListGatewayStartedShards listGatewayStartedShards,
			TransportNodesListShardStoreMetaData listShardStoreMetaData) {
		super(settings);
		this.listGatewayStartedShards = listGatewayStartedShards;
		this.listShardStoreMetaData = listShardStoreMetaData;

		this.listTimeout = componentSettings.getAsTime("list_timeout", TimeValue.timeValueSeconds(30));
		this.initialShards = componentSettings.get("initial_shards", "quorum");

		logger.debug("using initial_shards [{}], list_timeout [{}]", initialShards, listTimeout);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.allocator.GatewayAllocator#applyStartedShards(cn.com.summall.search.core.cluster.routing.allocation.StartedRerouteAllocation)
	 */
	@Override
	public void applyStartedShards(StartedRerouteAllocation allocation) {
		for (ShardRouting shardRouting : allocation.startedShards()) {
			cachedStores.remove(shardRouting.shardId());
			cachedShardsState.remove(shardRouting.shardId());
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.allocator.GatewayAllocator#applyFailedShards(cn.com.summall.search.core.cluster.routing.allocation.FailedRerouteAllocation)
	 */
	@Override
	public void applyFailedShards(FailedRerouteAllocation allocation) {
		ShardRouting failedShard = allocation.failedShard();
		cachedStores.remove(failedShard.shardId());
		cachedShardsState.remove(failedShard.shardId());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.allocator.GatewayAllocator#allocateUnassigned(cn.com.summall.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean allocateUnassigned(RoutingAllocation allocation) {
		boolean changed = false;
		DiscoveryNodes nodes = allocation.nodes();
		RoutingNodes routingNodes = allocation.routingNodes();

		
		Iterator<MutableShardRouting> unassignedIterator = routingNodes.unassigned().iterator();
		while (unassignedIterator.hasNext()) {
			MutableShardRouting shard = unassignedIterator.next();

			if (!shard.primary()) {
				continue;
			}

			
			if (!routingNodes.routingTable().index(shard.index()).shard(shard.id()).allocatedPostApi()) {
				continue;
			}

			TObjectLongHashMap<DiscoveryNode> nodesState = buildShardStates(nodes, shard);

			int numberOfAllocationsFound = 0;
			long highestVersion = -1;
			Set<DiscoveryNode> nodesWithHighestVersion = Sets.newHashSet();
			for (TObjectLongIterator<DiscoveryNode> it = nodesState.iterator(); it.hasNext();) {
				it.advance();
				DiscoveryNode node = it.key();
				long version = it.value();
				
				if (allocation.shouldIgnoreShardForNode(shard.shardId(), node.id())) {
					continue;
				}
				if (version != -1) {
					numberOfAllocationsFound++;
					if (highestVersion == -1) {
						nodesWithHighestVersion.add(node);
						highestVersion = version;
					} else {
						if (version > highestVersion) {
							nodesWithHighestVersion.clear();
							nodesWithHighestVersion.add(node);
							highestVersion = version;
						} else if (version == highestVersion) {
							nodesWithHighestVersion.add(node);
						}
					}
				}
			}

			
			int requiredAllocation = 1;
			try {
				IndexMetaData indexMetaData = routingNodes.metaData().index(shard.index());
				String initialShards = indexMetaData.settings()
						.get("index.recovery.initial_shards", this.initialShards);
				if ("quorum".equals(initialShards)) {
					if (indexMetaData.numberOfReplicas() > 1) {
						requiredAllocation = ((1 + indexMetaData.numberOfReplicas()) / 2) + 1;
					}
				} else if ("quorum-1".equals(initialShards) || "half".equals(initialShards)) {
					if (indexMetaData.numberOfReplicas() > 2) {
						requiredAllocation = ((1 + indexMetaData.numberOfReplicas()) / 2);
					}
				} else if ("one".equals(initialShards)) {
					requiredAllocation = 1;
				} else if ("full".equals(initialShards) || "all".equals(initialShards)) {
					requiredAllocation = indexMetaData.numberOfReplicas() + 1;
				} else if ("full-1".equals(initialShards) || "all-1".equals(initialShards)) {
					if (indexMetaData.numberOfReplicas() > 1) {
						requiredAllocation = indexMetaData.numberOfReplicas();
					}
				} else {
					requiredAllocation = Integer.parseInt(initialShards);
				}
			} catch (Exception e) {
				logger.warn("[" + shard.index() + "][" + shard.id() + "] failed to derived initial_shards from value "
						+ initialShards + ", ignore allocation for " + shard);
			}

			
			if (numberOfAllocationsFound < requiredAllocation) {
				
				unassignedIterator.remove();
				routingNodes.ignoredUnassigned().add(shard);
				if (logger.isDebugEnabled()) {
					logger.debug("[" + shard.index() + "][" + shard.id()
							+ "]: not allocating, number_of_allocated_shards_found [" + numberOfAllocationsFound
							+ "], required_number [" + requiredAllocation + "]");
				}
				continue;
			}

			Set<DiscoveryNode> throttledNodes = Sets.newHashSet();
			Set<DiscoveryNode> noNodes = Sets.newHashSet();
			for (DiscoveryNode discoNode : nodesWithHighestVersion) {
				RoutingNode node = routingNodes.node(discoNode.id());
				AllocationDecider.Decision decision = allocation.deciders().canAllocate(shard, node, allocation);
				if (decision == AllocationDecider.Decision.THROTTLE) {
					throttledNodes.add(discoNode);
				} else if (decision == AllocationDecider.Decision.NO) {
					noNodes.add(discoNode);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("[" + shard.index() + "][" + shard.id() + "]: allocating [" + shard + "] to ["
								+ discoNode + "] on primary allocation");
					}
					
					changed = true;
					
					node.add(new MutableShardRouting(shard, highestVersion));
					unassignedIterator.remove();

					
					throttledNodes.clear();
					noNodes.clear();
					break;
				}
			}
			if (throttledNodes.isEmpty()) {
				
				if (!noNodes.isEmpty()) {
					DiscoveryNode discoNode = noNodes.iterator().next();
					RoutingNode node = routingNodes.node(discoNode.id());
					if (logger.isDebugEnabled()) {
						logger.debug("[" + shard.index() + "][" + shard.id() + "]: forcing allocating [" + shard
								+ "] to [" + discoNode + "] on primary allocation");
					}
					
					changed = true;
					
					node.add(new MutableShardRouting(shard, highestVersion));
					unassignedIterator.remove();
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("[" + shard.index() + "][" + shard.id() + "]: throttling allocation [" + shard
							+ "] to [" + throttledNodes + "] on primary allocation");
				}
				
				unassignedIterator.remove();
				routingNodes.ignoredUnassigned().add(shard);
			}
		}

		if (!routingNodes.hasUnassigned()) {
			return changed;
		}

		
		unassignedIterator = routingNodes.unassigned().iterator();
		while (unassignedIterator.hasNext()) {
			MutableShardRouting shard = unassignedIterator.next();

			
			boolean canBeAllocatedToAtLeastOneNode = false;
			for (DiscoveryNode discoNode : nodes.dataNodes().values()) {
				RoutingNode node = routingNodes.node(discoNode.id());
				if (node == null) {
					continue;
				}
				
				
				if (allocation.deciders().canAllocate(shard, node, allocation).allocate()) {
					canBeAllocatedToAtLeastOneNode = true;
					break;
				}
			}

			if (!canBeAllocatedToAtLeastOneNode) {
				continue;
			}

			Map<DiscoveryNode, TransportNodesListShardStoreMetaData.StoreFilesMetaData> shardStores = buildShardStores(
					nodes, shard);

			long lastSizeMatched = 0;
			DiscoveryNode lastDiscoNodeMatched = null;
			RoutingNode lastNodeMatched = null;

			for (Map.Entry<DiscoveryNode, TransportNodesListShardStoreMetaData.StoreFilesMetaData> nodeStoreEntry : shardStores
					.entrySet()) {
				DiscoveryNode discoNode = nodeStoreEntry.getKey();
				TransportNodesListShardStoreMetaData.StoreFilesMetaData storeFilesMetaData = nodeStoreEntry.getValue();
				logger.trace("{}: checking node [{}]", shard, discoNode);

				if (storeFilesMetaData == null) {
					
					continue;
				}

				RoutingNode node = routingNodes.node(discoNode.id());
				if (node == null) {
					continue;
				}

				
				
				
				if (allocation.deciders().canAllocate(shard, node, allocation) == AllocationDecider.Decision.NO) {
					continue;
				}

				
				if (storeFilesMetaData.allocated()) {
					continue;
				}

				if (!shard.primary()) {
					MutableShardRouting primaryShard = routingNodes.findPrimaryForReplica(shard);
					if (primaryShard != null && primaryShard.active()) {
						DiscoveryNode primaryNode = nodes.get(primaryShard.currentNodeId());
						if (primaryNode != null) {
							TransportNodesListShardStoreMetaData.StoreFilesMetaData primaryNodeStore = shardStores
									.get(primaryNode);
							if (primaryNodeStore != null && primaryNodeStore.allocated()) {
								long sizeMatched = 0;

								for (StoreFileMetaData storeFileMetaData : storeFilesMetaData) {
									if (primaryNodeStore.fileExists(storeFileMetaData.name())
											&& primaryNodeStore.file(storeFileMetaData.name())
													.isSame(storeFileMetaData)) {
										sizeMatched += storeFileMetaData.length();
									}
								}
								if (sizeMatched > lastSizeMatched) {
									lastSizeMatched = sizeMatched;
									lastDiscoNodeMatched = discoNode;
									lastNodeMatched = node;
								}
							}
						}
					}
				}
			}

			if (lastNodeMatched != null) {
				
				if (allocation.deciders().canAllocate(shard, lastNodeMatched, allocation) == AllocationDecider.Decision.THROTTLE) {
					
					unassignedIterator.remove();
					routingNodes.ignoredUnassigned().add(shard);
				} else {
					
					changed = true;
					lastNodeMatched.add(shard);
					unassignedIterator.remove();
				}
			}
		}

		return changed;
	}

	
	/**
	 * Builds the shard states.
	 *
	 * @param nodes the nodes
	 * @param shard the shard
	 * @return the t object long hash map
	 */
	private TObjectLongHashMap<DiscoveryNode> buildShardStates(DiscoveryNodes nodes, MutableShardRouting shard) {
		TObjectLongHashMap<DiscoveryNode> shardStates = cachedShardsState.get(shard.shardId());
		Set<String> nodeIds;
		if (shardStates == null) {
			shardStates = new TObjectLongHashMap<DiscoveryNode>();
			cachedShardsState.put(shard.shardId(), shardStates);
			nodeIds = nodes.dataNodes().keySet();
		} else {
			
			for (TObjectLongIterator<DiscoveryNode> it = shardStates.iterator(); it.hasNext();) {
				it.advance();
				if (!nodes.nodeExists(it.key().id())) {
					it.remove();
				}
			}
			nodeIds = Sets.newHashSet();
			
			for (DiscoveryNode node : nodes.dataNodes().values()) {
				if (!shardStates.containsKey(node)) {
					nodeIds.add(node.id());
				}
			}
		}
		if (nodeIds.isEmpty()) {
			return shardStates;
		}

		TransportNodesListGatewayStartedShards.NodesLocalGatewayStartedShards response = listGatewayStartedShards.list(
				shard.shardId(), nodes.dataNodes().keySet(), listTimeout).actionGet();
		if (logger.isDebugEnabled()) {
			if (response.failures().length > 0) {
				StringBuilder sb = new StringBuilder(shard + ": failures when trying to list shards on nodes:");
				for (int i = 0; i < response.failures().length; i++) {
					Throwable cause = ExceptionsHelper.unwrapCause(response.failures()[i]);
					if (cause instanceof ConnectTransportException) {
						continue;
					}
					sb.append("\n    -> ").append(response.failures()[i].getDetailedMessage());
				}
				logger.debug(sb.toString());
			}
		}

		for (TransportNodesListGatewayStartedShards.NodeLocalGatewayStartedShards nodeShardState : response) {
			
			shardStates.put(nodeShardState.node(), nodeShardState.version());
		}
		return shardStates;
	}

	
	/**
	 * Builds the shard stores.
	 *
	 * @param nodes the nodes
	 * @param shard the shard
	 * @return the map
	 */
	private Map<DiscoveryNode, TransportNodesListShardStoreMetaData.StoreFilesMetaData> buildShardStores(
			DiscoveryNodes nodes, MutableShardRouting shard) {
		Map<DiscoveryNode, TransportNodesListShardStoreMetaData.StoreFilesMetaData> shardStores = cachedStores
				.get(shard.shardId());
		Set<String> nodesIds;
		if (shardStores == null) {
			shardStores = Maps.newHashMap();
			cachedStores.put(shard.shardId(), shardStores);
			nodesIds = nodes.dataNodes().keySet();
		} else {
			nodesIds = Sets.newHashSet();
			
			for (Iterator<DiscoveryNode> it = shardStores.keySet().iterator(); it.hasNext();) {
				DiscoveryNode node = it.next();
				if (!nodes.nodeExists(node.id())) {
					it.remove();
				}
			}

			for (DiscoveryNode node : nodes.dataNodes().values()) {
				if (!shardStores.containsKey(node)) {
					nodesIds.add(node.id());
				}
			}
		}

		if (!nodesIds.isEmpty()) {
			TransportNodesListShardStoreMetaData.NodesStoreFilesMetaData nodesStoreFilesMetaData = listShardStoreMetaData
					.list(shard.shardId(), false, nodesIds, listTimeout).actionGet();
			if (logger.isTraceEnabled()) {
				if (nodesStoreFilesMetaData.failures().length > 0) {
					StringBuilder sb = new StringBuilder(shard + ": failures when trying to list stores on nodes:");
					for (int i = 0; i < nodesStoreFilesMetaData.failures().length; i++) {
						Throwable cause = ExceptionsHelper.unwrapCause(nodesStoreFilesMetaData.failures()[i]);
						if (cause instanceof ConnectTransportException) {
							continue;
						}
						sb.append("\n    -> ").append(nodesStoreFilesMetaData.failures()[i].getDetailedMessage());
					}
					logger.trace(sb.toString());
				}
			}

			for (TransportNodesListShardStoreMetaData.NodeStoreFilesMetaData nodeStoreFilesMetaData : nodesStoreFilesMetaData) {
				if (nodeStoreFilesMetaData.storeFilesMetaData() != null) {
					shardStores.put(nodeStoreFilesMetaData.node(), nodeStoreFilesMetaData.storeFilesMetaData());
				}
			}
		}

		return shardStores;
	}
}
