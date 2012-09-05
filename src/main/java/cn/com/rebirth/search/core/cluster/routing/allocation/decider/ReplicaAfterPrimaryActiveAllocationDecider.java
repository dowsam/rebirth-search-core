/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ReplicaAfterPrimaryActiveAllocationDecider.java 2012-7-6 14:30:38 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.allocation.decider;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;

/**
 * The Class ReplicaAfterPrimaryActiveAllocationDecider.
 *
 * @author l.xue.nong
 */
public class ReplicaAfterPrimaryActiveAllocationDecider extends AllocationDecider {

	/**
	 * Instantiates a new replica after primary active allocation decider.
	 *
	 * @param settings the settings
	 */
	@Inject
	public ReplicaAfterPrimaryActiveAllocationDecider(Settings settings) {
		super(settings);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecider#canAllocate(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.cluster.routing.RoutingNode, cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public Decision canAllocate(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		if (shardRouting.primary()) {
			return Decision.YES;
		}
		MutableShardRouting primary = allocation.routingNodes().findPrimaryForReplica(shardRouting);
		if (primary == null || !primary.active()) {
			return Decision.NO;
		}
		return Decision.YES;
	}
}
