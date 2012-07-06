/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FailedRerouteAllocation.java 2012-3-29 15:01:21 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.allocation;

import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.RoutingNodes;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDeciders;


/**
 * The Class FailedRerouteAllocation.
 *
 * @author l.xue.nong
 */
public class FailedRerouteAllocation extends RoutingAllocation {

	
	/** The failed shard. */
	private final ShardRouting failedShard;

	
	/**
	 * Instantiates a new failed reroute allocation.
	 *
	 * @param deciders the deciders
	 * @param routingNodes the routing nodes
	 * @param nodes the nodes
	 * @param failedShard the failed shard
	 */
	public FailedRerouteAllocation(AllocationDeciders deciders, RoutingNodes routingNodes, DiscoveryNodes nodes,
			ShardRouting failedShard) {
		super(deciders, routingNodes, nodes);
		this.failedShard = failedShard;
	}

	
	/**
	 * Failed shard.
	 *
	 * @return the shard routing
	 */
	public ShardRouting failedShard() {
		return failedShard;
	}
}
