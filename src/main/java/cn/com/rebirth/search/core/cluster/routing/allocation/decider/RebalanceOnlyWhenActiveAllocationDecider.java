/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RebalanceOnlyWhenActiveAllocationDecider.java 2012-3-29 15:02:37 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.allocation.decider;

import java.util.List;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;


/**
 * The Class RebalanceOnlyWhenActiveAllocationDecider.
 *
 * @author l.xue.nong
 */
public class RebalanceOnlyWhenActiveAllocationDecider extends AllocationDecider {

	
	/**
	 * Instantiates a new rebalance only when active allocation decider.
	 *
	 * @param settings the settings
	 */
	@Inject
	public RebalanceOnlyWhenActiveAllocationDecider(Settings settings) {
		super(settings);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.decider.AllocationDecider#canRebalance(cn.com.summall.search.core.cluster.routing.ShardRouting, cn.com.summall.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean canRebalance(ShardRouting shardRouting, RoutingAllocation allocation) {
		List<MutableShardRouting> shards = allocation.routingNodes().shardsRoutingFor(shardRouting);
		
		
		for (int i = 0; i < shards.size(); i++) {
			if (!shards.get(i).active()) {
				return false;
			}
		}
		return true;
	}
}
