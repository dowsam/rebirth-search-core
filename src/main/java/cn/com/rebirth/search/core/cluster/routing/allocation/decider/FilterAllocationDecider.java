/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FilterAllocationDecider.java 2012-3-29 15:01:48 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.allocation.decider;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodeFilters;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.node.settings.NodeSettingsService;

import com.google.common.collect.ImmutableMap;


/**
 * The Class FilterAllocationDecider.
 *
 * @author l.xue.nong
 */
public class FilterAllocationDecider extends AllocationDecider {

    static {
        MetaData.addDynamicSettings(
                "cluster.routing.allocation.include.*",
                "cluster.routing.allocation.exclude.*"
        );
        IndexMetaData.addDynamicSettings(
                "index.routing.allocation.include.*",
                "index.routing.allocation.exclude.*"
        );
    }

    
    /** The cluster include filters. */
    private volatile DiscoveryNodeFilters clusterIncludeFilters;
    
    
    /** The cluster exclude filters. */
    private volatile DiscoveryNodeFilters clusterExcludeFilters;

    
    /**
     * Instantiates a new filter allocation decider.
     *
     * @param settings the settings
     * @param nodeSettingsService the node settings service
     */
    @Inject
    public FilterAllocationDecider(Settings settings, NodeSettingsService nodeSettingsService) {
        super(settings);
        ImmutableMap<String, String> includeMap = settings.getByPrefix("cluster.routing.allocation.include.").getAsMap();
        if (includeMap.isEmpty()) {
            clusterIncludeFilters = null;
        } else {
            clusterIncludeFilters = DiscoveryNodeFilters.buildFromKeyValue(includeMap);
        }
        ImmutableMap<String, String> excludeMap = settings.getByPrefix("cluster.routing.allocation.exclude.").getAsMap();
        if (excludeMap.isEmpty()) {
            clusterExcludeFilters = null;
        } else {
            clusterExcludeFilters = DiscoveryNodeFilters.buildFromKeyValue(excludeMap);
        }
        nodeSettingsService.addListener(new ApplySettings());
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.cluster.routing.allocation.decider.AllocationDecider#canAllocate(cn.com.summall.search.core.cluster.routing.ShardRouting, cn.com.summall.search.core.cluster.routing.RoutingNode, cn.com.summall.search.core.cluster.routing.allocation.RoutingAllocation)
     */
    @Override
    public Decision canAllocate(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
        return shouldFilter(shardRouting, node, allocation) ? Decision.NO : Decision.YES;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.cluster.routing.allocation.decider.AllocationDecider#canRemain(cn.com.summall.search.core.cluster.routing.ShardRouting, cn.com.summall.search.core.cluster.routing.RoutingNode, cn.com.summall.search.core.cluster.routing.allocation.RoutingAllocation)
     */
    @Override
    public boolean canRemain(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
        return !shouldFilter(shardRouting, node, allocation);
    }

    
    /**
     * Should filter.
     *
     * @param shardRouting the shard routing
     * @param node the node
     * @param allocation the allocation
     * @return true, if successful
     */
    private boolean shouldFilter(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
        if (clusterIncludeFilters != null) {
            if (!clusterIncludeFilters.match(node.node())) {
                return true;
            }
        }
        if (clusterExcludeFilters != null) {
            if (clusterExcludeFilters.match(node.node())) {
                return true;
            }
        }

        IndexMetaData indexMd = allocation.routingNodes().metaData().index(shardRouting.index());
        if (indexMd.includeFilters() != null) {
            if (!indexMd.includeFilters().match(node.node())) {
                return true;
            }
        }
        if (indexMd.excludeFilters() != null) {
            if (indexMd.excludeFilters().match(node.node())) {
                return true;
            }
        }

        return false;
    }

    
    /**
     * The Class ApplySettings.
     *
     * @author l.xue.nong
     */
    class ApplySettings implements NodeSettingsService.Listener {
        
        
        /* (non-Javadoc)
         * @see cn.com.summall.search.core.node.settings.NodeSettingsService.Listener#onRefreshSettings(cn.com.summall.search.commons.settings.Settings)
         */
        @Override
        public void onRefreshSettings(Settings settings) {
            ImmutableMap<String, String> includeMap = settings.getByPrefix("cluster.routing.allocation.include.").getAsMap();
            if (!includeMap.isEmpty()) {
                clusterIncludeFilters = DiscoveryNodeFilters.buildFromKeyValue(includeMap);
            }
            ImmutableMap<String, String> excludeMap = settings.getByPrefix("cluster.routing.allocation.exclude.").getAsMap();
            if (!excludeMap.isEmpty()) {
                clusterExcludeFilters = DiscoveryNodeFilters.buildFromKeyValue(excludeMap);
            }
        }
    }
}
