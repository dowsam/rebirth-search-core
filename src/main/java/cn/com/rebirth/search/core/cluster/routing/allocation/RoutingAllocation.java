/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RoutingAllocation.java 2012-3-29 15:02:39 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.allocation;

import java.util.HashMap;
import java.util.Map;

import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.RoutingNodes;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDeciders;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class RoutingAllocation.
 *
 * @author l.xue.nong
 */
public class RoutingAllocation {

    
    /**
     * The Class Result.
     *
     * @author l.xue.nong
     */
    public static class Result {

        
        /** The changed. */
        private final boolean changed;

        
        /** The routing table. */
        private final RoutingTable routingTable;

        
        /** The explanation. */
        private final AllocationExplanation explanation;

        
        /**
         * Instantiates a new result.
         *
         * @param changed the changed
         * @param routingTable the routing table
         * @param explanation the explanation
         */
        public Result(boolean changed, RoutingTable routingTable, AllocationExplanation explanation) {
            this.changed = changed;
            this.routingTable = routingTable;
            this.explanation = explanation;
        }

        
        /**
         * Changed.
         *
         * @return true, if successful
         */
        public boolean changed() {
            return this.changed;
        }

        
        /**
         * Routing table.
         *
         * @return the routing table
         */
        public RoutingTable routingTable() {
            return routingTable;
        }

        
        /**
         * Explanation.
         *
         * @return the allocation explanation
         */
        public AllocationExplanation explanation() {
            return explanation;
        }
    }

    
    /** The deciders. */
    private final AllocationDeciders deciders;

    
    /** The routing nodes. */
    private final RoutingNodes routingNodes;

    
    /** The nodes. */
    private final DiscoveryNodes nodes;

    
    /** The explanation. */
    private final AllocationExplanation explanation = new AllocationExplanation();

    
    /** The ignored shard to nodes. */
    private Map<ShardId, String> ignoredShardToNodes = null;

    
    /**
     * Instantiates a new routing allocation.
     *
     * @param deciders the deciders
     * @param routingNodes the routing nodes
     * @param nodes the nodes
     */
    public RoutingAllocation(AllocationDeciders deciders, RoutingNodes routingNodes, DiscoveryNodes nodes) {
        this.deciders = deciders;
        this.routingNodes = routingNodes;
        this.nodes = nodes;
    }

    
    /**
     * Deciders.
     *
     * @return the allocation deciders
     */
    public AllocationDeciders deciders() {
        return this.deciders;
    }

    
    /**
     * Routing table.
     *
     * @return the routing table
     */
    public RoutingTable routingTable() {
        return routingNodes.routingTable();
    }

    
    /**
     * Routing nodes.
     *
     * @return the routing nodes
     */
    public RoutingNodes routingNodes() {
        return routingNodes;
    }

    
    /**
     * Meta data.
     *
     * @return the meta data
     */
    public MetaData metaData() {
        return routingNodes.metaData();
    }

    
    /**
     * Nodes.
     *
     * @return the discovery nodes
     */
    public DiscoveryNodes nodes() {
        return nodes;
    }

    
    /**
     * Explanation.
     *
     * @return the allocation explanation
     */
    public AllocationExplanation explanation() {
        return explanation;
    }

    
    /**
     * Adds the ignore shard for node.
     *
     * @param shardId the shard id
     * @param nodeId the node id
     */
    public void addIgnoreShardForNode(ShardId shardId, String nodeId) {
        if (ignoredShardToNodes == null) {
            ignoredShardToNodes = new HashMap<ShardId, String>();
        }
        ignoredShardToNodes.put(shardId, nodeId);
    }

    
    /**
     * Should ignore shard for node.
     *
     * @param shardId the shard id
     * @param nodeId the node id
     * @return true, if successful
     */
    public boolean shouldIgnoreShardForNode(ShardId shardId, String nodeId) {
        return ignoredShardToNodes != null && nodeId.equals(ignoredShardToNodes.get(shardId));
    }
}
