/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GatewayAllocator.java 2012-3-29 15:01:03 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.allocation.allocator;

import cn.com.rebirth.search.core.cluster.routing.allocation.FailedRerouteAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.StartedRerouteAllocation;


/**
 * The Interface GatewayAllocator.
 *
 * @author l.xue.nong
 */
public interface GatewayAllocator {

	
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
}
