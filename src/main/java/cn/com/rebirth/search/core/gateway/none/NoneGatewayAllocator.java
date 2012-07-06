/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NoneGatewayAllocator.java 2012-7-6 14:29:40 l.xue.nong$$
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
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.allocator.GatewayAllocator#applyStartedShards(cn.com.rebirth.search.core.cluster.routing.allocation.StartedRerouteAllocation)
	 */
	@Override
	public void applyStartedShards(StartedRerouteAllocation allocation) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.allocator.GatewayAllocator#applyFailedShards(cn.com.rebirth.search.core.cluster.routing.allocation.FailedRerouteAllocation)
	 */
	@Override
	public void applyFailedShards(FailedRerouteAllocation allocation) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.allocator.GatewayAllocator#allocateUnassigned(cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean allocateUnassigned(RoutingAllocation allocation) {
		return false;
	}
}
