/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NoneGatewayAllocator.java 2012-3-29 15:02:19 l.xue.nong$$
 */


package cn.com.rebirth.search.core.gateway.none;

import cn.com.rebirth.search.core.cluster.routing.allocation.FailedRerouteAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.StartedRerouteAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.allocator.GatewayAllocator;


/**
 * The Class NoneGatewayAllocator.
 *
 * @author l.xue.nong
 */
public class NoneGatewayAllocator implements GatewayAllocator {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.allocator.GatewayAllocator#applyStartedShards(cn.com.summall.search.core.cluster.routing.allocation.StartedRerouteAllocation)
	 */
	@Override
	public void applyStartedShards(StartedRerouteAllocation allocation) {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.allocator.GatewayAllocator#applyFailedShards(cn.com.summall.search.core.cluster.routing.allocation.FailedRerouteAllocation)
	 */
	@Override
	public void applyFailedShards(FailedRerouteAllocation allocation) {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.allocator.GatewayAllocator#allocateUnassigned(cn.com.summall.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean allocateUnassigned(RoutingAllocation allocation) {
		return false;
	}
}
