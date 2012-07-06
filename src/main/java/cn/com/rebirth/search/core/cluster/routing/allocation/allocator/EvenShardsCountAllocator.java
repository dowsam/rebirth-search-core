/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core EvenShardsCountAllocator.java 2012-3-29 15:01:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.allocation.allocator;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.RoutingNodes;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.ShardRoutingState;
import cn.com.rebirth.search.core.cluster.routing.allocation.FailedRerouteAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.StartedRerouteAllocation;


/**
 * The Class EvenShardsCountAllocator.
 *
 * @author l.xue.nong
 */
public class EvenShardsCountAllocator extends AbstractComponent implements ShardsAllocator {

	
	/**
	 * Instantiates a new even shards count allocator.
	 *
	 * @param settings the settings
	 */
	@Inject
	public EvenShardsCountAllocator(Settings settings) {
		super(settings);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.allocator.ShardsAllocator#applyStartedShards(cn.com.summall.search.core.cluster.routing.allocation.StartedRerouteAllocation)
	 */
	@Override
	public void applyStartedShards(StartedRerouteAllocation allocation) {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.allocator.ShardsAllocator#applyFailedShards(cn.com.summall.search.core.cluster.routing.allocation.FailedRerouteAllocation)
	 */
	@Override
	public void applyFailedShards(FailedRerouteAllocation allocation) {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.allocator.ShardsAllocator#allocateUnassigned(cn.com.summall.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean allocateUnassigned(RoutingAllocation allocation) {
		boolean changed = false;
		RoutingNodes routingNodes = allocation.routingNodes();

		RoutingNode[] nodes = sortedNodesLeastToHigh(allocation);

		Iterator<MutableShardRouting> unassignedIterator = routingNodes.unassigned().iterator();
		int lastNode = 0;

		while (unassignedIterator.hasNext()) {
			MutableShardRouting shard = unassignedIterator.next();
			
			for (int i = 0; i < nodes.length; i++) {
				RoutingNode node = nodes[lastNode];
				lastNode++;
				if (lastNode == nodes.length) {
					lastNode = 0;
				}

				if (allocation.deciders().canAllocate(shard, node, allocation).allocate()) {
					int numberOfShardsToAllocate = routingNodes.requiredAverageNumberOfShardsPerNode()
							- node.shards().size();
					if (numberOfShardsToAllocate <= 0) {
						continue;
					}

					changed = true;
					node.add(shard);
					unassignedIterator.remove();
					break;
				}
			}
		}

		
		for (Iterator<MutableShardRouting> it = routingNodes.unassigned().iterator(); it.hasNext();) {
			MutableShardRouting shard = it.next();
			
			for (RoutingNode routingNode : sortedNodesLeastToHigh(allocation)) {
				if (allocation.deciders().canAllocate(shard, routingNode, allocation).allocate()) {
					changed = true;
					routingNode.add(shard);
					it.remove();
					break;
				}
			}
		}
		return changed;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.allocator.ShardsAllocator#rebalance(cn.com.summall.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean rebalance(RoutingAllocation allocation) {
		boolean changed = false;
		RoutingNode[] sortedNodesLeastToHigh = sortedNodesLeastToHigh(allocation);
		if (sortedNodesLeastToHigh.length == 0) {
			return false;
		}
		int lowIndex = 0;
		int highIndex = sortedNodesLeastToHigh.length - 1;
		boolean relocationPerformed;
		do {
			relocationPerformed = false;
			while (lowIndex != highIndex) {
				RoutingNode lowRoutingNode = sortedNodesLeastToHigh[lowIndex];
				RoutingNode highRoutingNode = sortedNodesLeastToHigh[highIndex];
				int averageNumOfShards = allocation.routingNodes().requiredAverageNumberOfShardsPerNode();

				
				if (highRoutingNode.numberOfOwningShards() <= averageNumOfShards) {
					highIndex--;
					continue;
				}

				if (lowRoutingNode.shards().size() >= averageNumOfShards) {
					lowIndex++;
					continue;
				}

				boolean relocated = false;
				List<MutableShardRouting> startedShards = highRoutingNode.shardsWithState(ShardRoutingState.STARTED);
				for (MutableShardRouting startedShard : startedShards) {
					if (!allocation.deciders().canRebalance(startedShard, allocation)) {
						continue;
					}

					if (allocation.deciders().canAllocate(startedShard, lowRoutingNode, allocation).allocate()) {
						changed = true;
						lowRoutingNode.add(new MutableShardRouting(startedShard.index(), startedShard.id(),
								lowRoutingNode.nodeId(), startedShard.currentNodeId(), startedShard.primary(),
								ShardRoutingState.INITIALIZING, startedShard.version() + 1));

						startedShard.relocate(lowRoutingNode.nodeId());
						relocated = true;
						relocationPerformed = true;
						break;
					}
				}

				if (!relocated) {
					highIndex--;
				}
			}
		} while (relocationPerformed);
		return changed;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.allocator.ShardsAllocator#move(cn.com.summall.search.core.cluster.routing.MutableShardRouting, cn.com.summall.search.core.cluster.routing.RoutingNode, cn.com.summall.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean move(MutableShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		assert shardRouting.started();
		boolean changed = false;
		RoutingNode[] sortedNodesLeastToHigh = sortedNodesLeastToHigh(allocation);
		if (sortedNodesLeastToHigh.length == 0) {
			return false;
		}

		for (RoutingNode nodeToCheck : sortedNodesLeastToHigh) {
			
			if (nodeToCheck.nodeId().equals(node.nodeId())) {
				continue;
			}
			if (allocation.deciders().canAllocate(shardRouting, nodeToCheck, allocation).allocate()) {
				nodeToCheck.add(new MutableShardRouting(shardRouting.index(), shardRouting.id(), nodeToCheck.nodeId(),
						shardRouting.currentNodeId(), shardRouting.primary(), ShardRoutingState.INITIALIZING,
						shardRouting.version() + 1));

				shardRouting.relocate(nodeToCheck.nodeId());
				changed = true;
				break;
			}
		}

		return changed;
	}

	
	/**
	 * Sorted nodes least to high.
	 *
	 * @param allocation the allocation
	 * @return the routing node[]
	 */
	private RoutingNode[] sortedNodesLeastToHigh(RoutingAllocation allocation) {
		
		final TObjectIntHashMap<String> nodeCounts = new TObjectIntHashMap<String>();
		for (RoutingNode node : allocation.routingNodes()) {
			for (int i = 0; i < node.shards().size(); i++) {
				ShardRouting shardRouting = node.shards().get(i);
				String nodeId = shardRouting.relocating() ? shardRouting.relocatingNodeId() : shardRouting
						.currentNodeId();
				nodeCounts.adjustOrPutValue(nodeId, 1, 1);
			}
		}
		RoutingNode[] nodes = allocation.routingNodes().nodesToShards().values()
				.toArray(new RoutingNode[allocation.routingNodes().nodesToShards().values().size()]);
		Arrays.sort(nodes, new Comparator<RoutingNode>() {
			@Override
			public int compare(RoutingNode o1, RoutingNode o2) {
				return nodeCounts.get(o1.nodeId()) - nodeCounts.get(o2.nodeId());
			}
		});
		return nodes;
	}
}
