/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AllocationDeciders.java 2012-7-6 14:29:26 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.allocation.decider;

import java.util.Set;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.node.settings.NodeSettingsService;

import com.google.common.collect.ImmutableSet;

/**
 * The Class AllocationDeciders.
 *
 * @author l.xue.nong
 */
public class AllocationDeciders extends AllocationDecider {

	/** The allocations. */
	private final AllocationDecider[] allocations;

	/**
	 * Instantiates a new allocation deciders.
	 *
	 * @param settings the settings
	 * @param nodeSettingsService the node settings service
	 */
	public AllocationDeciders(Settings settings, NodeSettingsService nodeSettingsService) {
		this(settings, ImmutableSet.<AllocationDecider> builder().add(new SameShardAllocationDecider(settings))
				.add(new FilterAllocationDecider(settings, nodeSettingsService))
				.add(new ReplicaAfterPrimaryActiveAllocationDecider(settings))
				.add(new ThrottlingAllocationDecider(settings, nodeSettingsService))
				.add(new RebalanceOnlyWhenActiveAllocationDecider(settings))
				.add(new ClusterRebalanceAllocationDecider(settings))
				.add(new ConcurrentRebalanceAllocationDecider(settings, nodeSettingsService))
				.add(new DisableAllocationDecider(settings, nodeSettingsService))
				.add(new AwarenessAllocationDecider(settings, nodeSettingsService))
				.add(new ShardsLimitAllocationDecider(settings)).build());
	}

	/**
	 * Instantiates a new allocation deciders.
	 *
	 * @param settings the settings
	 * @param allocations the allocations
	 */
	@Inject
	public AllocationDeciders(Settings settings, Set<AllocationDecider> allocations) {
		super(settings);
		this.allocations = allocations.toArray(new AllocationDecider[allocations.size()]);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecider#canRebalance(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean canRebalance(ShardRouting shardRouting, RoutingAllocation allocation) {
		for (AllocationDecider allocation1 : allocations) {
			if (!allocation1.canRebalance(shardRouting, allocation)) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecider#canAllocate(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.cluster.routing.RoutingNode, cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public Decision canAllocate(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		Decision ret = Decision.YES;

		if (allocation.shouldIgnoreShardForNode(shardRouting.shardId(), node.nodeId())) {
			return Decision.NO;
		}

		for (AllocationDecider allocation1 : allocations) {
			Decision decision = allocation1.canAllocate(shardRouting, node, allocation);
			if (decision == Decision.NO) {
				return Decision.NO;
			} else if (decision == Decision.THROTTLE) {
				ret = Decision.THROTTLE;
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecider#canRemain(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.cluster.routing.RoutingNode, cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean canRemain(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		if (allocation.shouldIgnoreShardForNode(shardRouting.shardId(), node.nodeId())) {
			return false;
		}
		for (AllocationDecider allocation1 : allocations) {
			if (!allocation1.canRemain(shardRouting, node, allocation)) {
				return false;
			}
		}
		return true;
	}
}
