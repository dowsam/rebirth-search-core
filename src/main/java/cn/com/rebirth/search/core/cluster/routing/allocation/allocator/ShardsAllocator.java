/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardsAllocator.java 2012-3-29 15:01:23 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.allocation.allocator;

import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.allocation.FailedRerouteAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.StartedRerouteAllocation;


/**
 * The Interface ShardsAllocator.
 *
 * @author l.xue.nong
 */
public interface ShardsAllocator {

	
	/**
	 * Apply started shards.
	 *
	 * @param allocation the allocation
	 */
	void applyStartedShards(StartedRerouteAllocation allocation);

	
	/**
	 * Apply failed shards.
	 *
	 * @param allocation the allocation
	 */
	void applyFailedShards(FailedRerouteAllocation allocation);

	
	/**
	 * Allocate unassigned.
	 *
	 * @param allocation the allocation
	 * @return true, if successful
	 */
	boolean allocateUnassigned(RoutingAllocation allocation);

	
	/**
	 * Rebalance.
	 *
	 * @param allocation the allocation
	 * @return true, if successful
	 */
	boolean rebalance(RoutingAllocation allocation);

	
	/**
	 * Move.
	 *
	 * @param shardRouting the shard routing
	 * @param node the node
	 * @param allocation the allocation
	 * @return true, if successful
	 */
	boolean move(MutableShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation);
}
