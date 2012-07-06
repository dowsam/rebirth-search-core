/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SameShardAllocationDecider.java 2012-3-29 15:01:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.allocation.decider;

import java.util.List;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;


/**
 * The Class SameShardAllocationDecider.
 *
 * @author l.xue.nong
 */
public class SameShardAllocationDecider extends AllocationDecider {

	
	/** The Constant SAME_HOST_SETTING. */
	public static final String SAME_HOST_SETTING = "cluster.routing.allocation.same_shard.host";

	
	/** The same host. */
	private final boolean sameHost;

	
	/**
	 * Instantiates a new same shard allocation decider.
	 *
	 * @param settings the settings
	 */
	@Inject
	public SameShardAllocationDecider(Settings settings) {
		super(settings);

		this.sameHost = settings.getAsBoolean(SAME_HOST_SETTING, false);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.decider.AllocationDecider#canAllocate(cn.com.summall.search.core.cluster.routing.ShardRouting, cn.com.summall.search.core.cluster.routing.RoutingNode, cn.com.summall.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public Decision canAllocate(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		List<MutableShardRouting> shards = node.shards();
		for (int i = 0; i < shards.size(); i++) {
			
			if (shards.get(i).shardId().equals(shardRouting.shardId())) {
				return Decision.NO;
			}
		}
		if (sameHost) {
			if (node.node() != null) {
				for (RoutingNode checkNode : allocation.routingNodes()) {
					if (checkNode.node() == null) {
						continue;
					}
					
					if (!checkNode.node().address().sameHost(node.node().address())) {
						continue;
					}
					shards = checkNode.shards();
					for (int i = 0; i < shards.size(); i++) {
						if (shards.get(i).shardId().equals(shardRouting.shardId())) {
							return Decision.NO;
						}
					}
				}
			}
		}
		return Decision.YES;
	}
}
