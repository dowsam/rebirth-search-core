/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AllocationService.java 2012-7-6 14:29:30 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.allocation;

import static com.google.common.collect.Sets.newHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.routing.IndexRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.RoutingNodes;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.ShardRoutingState;
import cn.com.rebirth.search.core.cluster.routing.allocation.allocator.ShardsAllocators;
import cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDeciders;
import cn.com.rebirth.search.core.node.settings.NodeSettingsService;

import com.google.common.collect.Lists;

/**
 * The Class AllocationService.
 *
 * @author l.xue.nong
 */
public class AllocationService extends AbstractComponent {

	/** The allocation deciders. */
	private final AllocationDeciders allocationDeciders;

	/** The shards allocators. */
	private final ShardsAllocators shardsAllocators;

	/**
	 * Instantiates a new allocation service.
	 */
	public AllocationService() {
		this(ImmutableSettings.Builder.EMPTY_SETTINGS);
	}

	/**
	 * Instantiates a new allocation service.
	 *
	 * @param settings the settings
	 */
	public AllocationService(Settings settings) {
		this(settings, new AllocationDeciders(settings, new NodeSettingsService(
				ImmutableSettings.Builder.EMPTY_SETTINGS)), new ShardsAllocators(settings));
	}

	/**
	 * Instantiates a new allocation service.
	 *
	 * @param settings the settings
	 * @param allocationDeciders the allocation deciders
	 * @param shardsAllocators the shards allocators
	 */
	@Inject
	public AllocationService(Settings settings, AllocationDeciders allocationDeciders, ShardsAllocators shardsAllocators) {
		super(settings);
		this.allocationDeciders = allocationDeciders;
		this.shardsAllocators = shardsAllocators;
	}

	/**
	 * Apply started shards.
	 *
	 * @param clusterState the cluster state
	 * @param startedShards the started shards
	 * @return the routing allocation. result
	 */
	public RoutingAllocation.Result applyStartedShards(ClusterState clusterState,
			List<? extends ShardRouting> startedShards) {
		RoutingNodes routingNodes = clusterState.routingNodes();

		Collections.shuffle(routingNodes.unassigned());
		StartedRerouteAllocation allocation = new StartedRerouteAllocation(allocationDeciders, routingNodes,
				clusterState.nodes(), startedShards);
		boolean changed = applyStartedShards(routingNodes, startedShards);
		if (!changed) {
			return new RoutingAllocation.Result(false, clusterState.routingTable(), allocation.explanation());
		}
		shardsAllocators.applyStartedShards(allocation);
		reroute(allocation);
		return new RoutingAllocation.Result(true, new RoutingTable.Builder().updateNodes(routingNodes).build()
				.validateRaiseException(clusterState.metaData()), allocation.explanation());
	}

	/**
	 * Apply failed shard.
	 *
	 * @param clusterState the cluster state
	 * @param failedShard the failed shard
	 * @return the routing allocation. result
	 */
	public RoutingAllocation.Result applyFailedShard(ClusterState clusterState, ShardRouting failedShard) {
		RoutingNodes routingNodes = clusterState.routingNodes();

		Collections.shuffle(routingNodes.unassigned());
		FailedRerouteAllocation allocation = new FailedRerouteAllocation(allocationDeciders, routingNodes,
				clusterState.nodes(), failedShard);
		boolean changed = applyFailedShard(allocation);
		if (!changed) {
			return new RoutingAllocation.Result(false, clusterState.routingTable(), allocation.explanation());
		}
		shardsAllocators.applyFailedShards(allocation);
		reroute(allocation);
		return new RoutingAllocation.Result(true, new RoutingTable.Builder().updateNodes(routingNodes).build()
				.validateRaiseException(clusterState.metaData()), allocation.explanation());
	}

	/**
	 * Reroute.
	 *
	 * @param clusterState the cluster state
	 * @return the routing allocation. result
	 */
	public RoutingAllocation.Result reroute(ClusterState clusterState) {
		RoutingNodes routingNodes = clusterState.routingNodes();

		Collections.shuffle(routingNodes.unassigned());
		RoutingAllocation allocation = new RoutingAllocation(allocationDeciders, routingNodes, clusterState.nodes());
		if (!reroute(allocation)) {
			return new RoutingAllocation.Result(false, clusterState.routingTable(), allocation.explanation());
		}
		return new RoutingAllocation.Result(true, new RoutingTable.Builder().updateNodes(routingNodes).build()
				.validateRaiseException(clusterState.metaData()), allocation.explanation());
	}

	/**
	 * Reroute with no reassign.
	 *
	 * @param clusterState the cluster state
	 * @return the routing allocation. result
	 */
	public RoutingAllocation.Result rerouteWithNoReassign(ClusterState clusterState) {
		RoutingNodes routingNodes = clusterState.routingNodes();

		Collections.shuffle(routingNodes.unassigned());
		RoutingAllocation allocation = new RoutingAllocation(allocationDeciders, routingNodes, clusterState.nodes());
		Iterable<DiscoveryNode> dataNodes = allocation.nodes().dataNodes().values();
		boolean changed = false;

		changed |= deassociateDeadNodes(allocation.routingNodes(), dataNodes);

		applyNewNodes(allocation.routingNodes(), dataNodes);

		changed |= electPrimaries(allocation.routingNodes());

		if (!changed) {
			return new RoutingAllocation.Result(false, clusterState.routingTable(), allocation.explanation());
		}
		return new RoutingAllocation.Result(true, new RoutingTable.Builder().updateNodes(routingNodes).build()
				.validateRaiseException(clusterState.metaData()), allocation.explanation());
	}

	/**
	 * Reroute.
	 *
	 * @param allocation the allocation
	 * @return true, if successful
	 */
	private boolean reroute(RoutingAllocation allocation) {
		Iterable<DiscoveryNode> dataNodes = allocation.nodes().dataNodes().values();

		boolean changed = false;

		changed |= deassociateDeadNodes(allocation.routingNodes(), dataNodes);

		applyNewNodes(allocation.routingNodes(), dataNodes);

		changed |= electPrimaries(allocation.routingNodes());

		if (allocation.routingNodes().hasUnassigned()) {
			changed |= shardsAllocators.allocateUnassigned(allocation);

			changed |= electPrimaries(allocation.routingNodes());
		}

		changed |= moveShards(allocation);

		changed |= shardsAllocators.rebalance(allocation);

		return changed;
	}

	/**
	 * Move shards.
	 *
	 * @param allocation the allocation
	 * @return true, if successful
	 */
	private boolean moveShards(RoutingAllocation allocation) {
		boolean changed = false;

		List<MutableShardRouting> shards = new ArrayList<MutableShardRouting>();
		int index = 0;
		boolean found = true;
		while (found) {
			found = false;
			for (RoutingNode routingNode : allocation.routingNodes()) {
				if (index >= routingNode.shards().size()) {
					continue;
				}
				found = true;
				shards.add(routingNode.shards().get(index));
			}
			index++;
		}
		for (int i = 0; i < shards.size(); i++) {
			MutableShardRouting shardRouting = shards.get(i);

			if (!shardRouting.started()) {
				continue;
			}
			RoutingNode routingNode = allocation.routingNodes().node(shardRouting.currentNodeId());
			if (!allocation.deciders().canRemain(shardRouting, routingNode, allocation)) {
				logger.debug("[" + shardRouting.index()
						+ "][{}] allocated on [{}], but can no longer be allocated on it, moving...",
						shardRouting.id(), routingNode.node());
				boolean moved = shardsAllocators.move(shardRouting, routingNode, allocation);
				if (!moved) {
					logger.debug("[{}][{}] can't move", shardRouting.index(), shardRouting.id());
				} else {
					changed = true;
				}
			}
		}
		return changed;
	}

	/**
	 * Elect primaries.
	 *
	 * @param routingNodes the routing nodes
	 * @return true, if successful
	 */
	private boolean electPrimaries(RoutingNodes routingNodes) {
		boolean changed = false;
		for (MutableShardRouting shardEntry : routingNodes.unassigned()) {
			if (shardEntry.primary() && !shardEntry.assignedToNode()) {
				boolean elected = false;

				for (RoutingNode routingNode : routingNodes.nodesToShards().values()) {

					for (MutableShardRouting shardEntry2 : routingNode.shards()) {
						if (shardEntry.shardId().equals(shardEntry2.shardId()) && shardEntry2.active()) {
							assert shardEntry2.assignedToNode();
							assert !shardEntry2.primary();

							changed = true;
							shardEntry.moveFromPrimary();
							shardEntry2.moveToPrimary();
							elected = true;
							break;
						}
					}

					if (elected) {
						break;
					}
				}
			}
		}
		return changed;
	}

	/**
	 * Apply new nodes.
	 *
	 * @param routingNodes the routing nodes
	 * @param liveNodes the live nodes
	 */
	private void applyNewNodes(RoutingNodes routingNodes, Iterable<DiscoveryNode> liveNodes) {
		for (DiscoveryNode node : liveNodes) {
			if (!routingNodes.nodesToShards().containsKey(node.id())) {
				RoutingNode routingNode = new RoutingNode(node.id(), node);
				routingNodes.nodesToShards().put(node.id(), routingNode);
			}
		}
	}

	/**
	 * Deassociate dead nodes.
	 *
	 * @param routingNodes the routing nodes
	 * @param liveNodes the live nodes
	 * @return true, if successful
	 */
	private boolean deassociateDeadNodes(RoutingNodes routingNodes, Iterable<DiscoveryNode> liveNodes) {
		boolean changed = false;
		Set<String> liveNodeIds = newHashSet();
		for (DiscoveryNode liveNode : liveNodes) {
			liveNodeIds.add(liveNode.id());
		}
		Set<String> nodeIdsToRemove = newHashSet();
		for (RoutingNode routingNode : routingNodes) {
			for (Iterator<MutableShardRouting> shardsIterator = routingNode.shards().iterator(); shardsIterator
					.hasNext();) {
				MutableShardRouting shardRoutingEntry = shardsIterator.next();
				if (shardRoutingEntry.assignedToNode()) {

					boolean relocating = shardRoutingEntry.relocating();
					String relocatingNodeId = shardRoutingEntry.relocatingNodeId();

					boolean isRelocationDestinationShard = relocatingNodeId != null && shardRoutingEntry.initializing();

					boolean currentNodeIsDead = false;
					if (!liveNodeIds.contains(shardRoutingEntry.currentNodeId())) {
						changed = true;
						nodeIdsToRemove.add(shardRoutingEntry.currentNodeId());

						if (!isRelocationDestinationShard) {
							routingNodes.unassigned().add(shardRoutingEntry);
						}

						shardRoutingEntry.deassignNode();
						currentNodeIsDead = true;
						shardsIterator.remove();
					}

					if (relocating && !liveNodeIds.contains(relocatingNodeId)) {
						nodeIdsToRemove.add(relocatingNodeId);
						if (!currentNodeIsDead) {
							changed = true;
							shardRoutingEntry.cancelRelocation();
						}
					}

					if (isRelocationDestinationShard && !liveNodeIds.contains(relocatingNodeId)) {
						changed = true;
						shardsIterator.remove();
					}
				}
			}
		}
		for (String nodeIdToRemove : nodeIdsToRemove) {
			routingNodes.nodesToShards().remove(nodeIdToRemove);
		}
		return changed;
	}

	/**
	 * Apply started shards.
	 *
	 * @param routingNodes the routing nodes
	 * @param startedShardEntries the started shard entries
	 * @return true, if successful
	 */
	private boolean applyStartedShards(RoutingNodes routingNodes, Iterable<? extends ShardRouting> startedShardEntries) {
		boolean dirty = false;

		for (ShardRouting startedShard : startedShardEntries) {
			assert startedShard.state() == ShardRoutingState.INITIALIZING;

			String relocatingNodeId = null;

			RoutingNode currentRoutingNode = routingNodes.nodesToShards().get(startedShard.currentNodeId());
			if (currentRoutingNode != null) {
				for (MutableShardRouting shard : currentRoutingNode) {
					if (shard.shardId().equals(startedShard.shardId())) {
						relocatingNodeId = shard.relocatingNodeId();
						if (!shard.started()) {
							dirty = true;
							shard.moveToStarted();
						}
						break;
					}
				}
			}

			if (relocatingNodeId == null)
				continue;

			RoutingNode sourceRoutingNode = routingNodes.nodesToShards().get(relocatingNodeId);
			if (sourceRoutingNode != null) {
				Iterator<MutableShardRouting> shardsIter = sourceRoutingNode.iterator();
				while (shardsIter.hasNext()) {
					MutableShardRouting shard = shardsIter.next();
					if (shard.shardId().equals(startedShard.shardId())) {
						if (shard.relocating()) {
							dirty = true;
							shardsIter.remove();
							break;
						}
					}
				}
			}
		}
		return dirty;
	}

	/**
	 * Apply failed shard.
	 *
	 * @param allocation the allocation
	 * @return true, if successful
	 */
	private boolean applyFailedShard(FailedRerouteAllocation allocation) {
		IndexRoutingTable indexRoutingTable = allocation.routingTable().index(allocation.failedShard().index());
		if (indexRoutingTable == null) {
			return false;
		}

		ShardRouting failedShard = allocation.failedShard();

		boolean shardDirty = false;
		boolean inRelocation = failedShard.relocatingNodeId() != null;
		if (inRelocation) {
			RoutingNode routingNode = allocation.routingNodes().nodesToShards().get(failedShard.currentNodeId());
			if (routingNode != null) {
				Iterator<MutableShardRouting> shards = routingNode.iterator();
				while (shards.hasNext()) {
					MutableShardRouting shard = shards.next();
					if (shard.shardId().equals(failedShard.shardId())) {
						shardDirty = true;
						shard.deassignNode();
						shards.remove();
						break;
					}
				}
			}
		}

		String nodeId = inRelocation ? failedShard.relocatingNodeId() : failedShard.currentNodeId();
		RoutingNode currentRoutingNode = allocation.routingNodes().nodesToShards().get(nodeId);

		if (currentRoutingNode == null) {

			return false;
		}

		Iterator<MutableShardRouting> shards = currentRoutingNode.iterator();
		while (shards.hasNext()) {
			MutableShardRouting shard = shards.next();
			if (shard.shardId().equals(failedShard.shardId())) {
				shardDirty = true;
				if (!inRelocation) {
					shard.deassignNode();
					shards.remove();
				} else {
					shard.cancelRelocation();
				}
				break;
			}
		}

		if (!shardDirty) {
			return false;
		}

		allocation.addIgnoreShardForNode(failedShard.shardId(), failedShard.currentNodeId());

		if (inRelocation) {
			return true;
		}

		List<MutableShardRouting> shardsToMove = Lists.newArrayList();
		for (Iterator<MutableShardRouting> it = allocation.routingNodes().unassigned().iterator(); it.hasNext();) {
			MutableShardRouting shardRouting = it.next();
			if (shardRouting.shardId().equals(failedShard.shardId())) {
				it.remove();
				shardsToMove.add(shardRouting);
			}
		}
		if (!shardsToMove.isEmpty()) {
			allocation.routingNodes().unassigned().addAll(shardsToMove);
		}

		allocation
				.routingNodes()
				.unassigned()
				.add(new MutableShardRouting(failedShard.index(), failedShard.id(), null, failedShard.primary(),
						ShardRoutingState.UNASSIGNED, failedShard.version() + 1));

		return true;
	}
}
