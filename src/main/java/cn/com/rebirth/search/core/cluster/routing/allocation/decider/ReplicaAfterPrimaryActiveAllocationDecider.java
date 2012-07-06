/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ReplicaAfterPrimaryActiveAllocationDecider.java 2012-3-29 15:01:25 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.allocation.decider;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
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
     * @see cn.com.summall.search.core.cluster.routing.allocation.decider.AllocationDecider#canAllocate(cn.com.summall.search.core.cluster.routing.ShardRouting, cn.com.summall.search.core.cluster.routing.RoutingNode, cn.com.summall.search.core.cluster.routing.allocation.RoutingAllocation)
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
