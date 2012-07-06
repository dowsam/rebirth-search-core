/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StartedRerouteAllocation.java 2012-7-6 14:28:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.allocation;

import java.util.List;

import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.RoutingNodes;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDeciders;

/**
 * The Class StartedRerouteAllocation.
 *
 * @author l.xue.nong
 */
public class StartedRerouteAllocation extends RoutingAllocation {

	/** The started shards. */
	private final List<? extends ShardRouting> startedShards;

	/**
	 * Instantiates a new started reroute allocation.
	 *
	 * @param deciders the deciders
	 * @param routingNodes the routing nodes
	 * @param nodes the nodes
	 * @param startedShards the started shards
	 */
	public StartedRerouteAllocation(AllocationDeciders deciders, RoutingNodes routingNodes, DiscoveryNodes nodes,
			List<? extends ShardRouting> startedShards) {
		super(deciders, routingNodes, nodes);
		this.startedShards = startedShards;
	}

	/**
	 * Started shards.
	 *
	 * @return the list<? extends shard routing>
	 */
	public List<? extends ShardRouting> startedShards() {
		return startedShards;
	}
}
