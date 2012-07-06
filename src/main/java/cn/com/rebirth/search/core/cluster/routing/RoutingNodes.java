/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RoutingNodes.java 2012-3-29 15:02:27 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlocks;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;


/**
 * The Class RoutingNodes.
 *
 * @author l.xue.nong
 */
public class RoutingNodes implements Iterable<RoutingNode> {

    
    /** The meta data. */
    private final MetaData metaData;

    
    /** The blocks. */
    private final ClusterBlocks blocks;

    
    /** The routing table. */
    private final RoutingTable routingTable;

    
    /** The nodes to shards. */
    private final Map<String, RoutingNode> nodesToShards = newHashMap();

    
    /** The unassigned. */
    private final List<MutableShardRouting> unassigned = newArrayList();

    
    /** The ignored unassigned. */
    private final List<MutableShardRouting> ignoredUnassigned = newArrayList();

    
    /** The nodes per attribute names. */
    private final Map<String, TObjectIntHashMap<String>> nodesPerAttributeNames = new HashMap<String, TObjectIntHashMap<String>>();

    
    /**
     * Instantiates a new routing nodes.
     *
     * @param clusterState the cluster state
     */
    public RoutingNodes(ClusterState clusterState) {
        this.metaData = clusterState.metaData();
        this.blocks = clusterState.blocks();
        this.routingTable = clusterState.routingTable();
        Map<String, List<MutableShardRouting>> nodesToShards = newHashMap();
        for (IndexRoutingTable indexRoutingTable : routingTable.indicesRouting().values()) {
            for (IndexShardRoutingTable indexShard : indexRoutingTable) {
                for (ShardRouting shard : indexShard) {
                    if (shard.assignedToNode()) {
                        List<MutableShardRouting> entries = nodesToShards.get(shard.currentNodeId());
                        if (entries == null) {
                            entries = newArrayList();
                            nodesToShards.put(shard.currentNodeId(), entries);
                        }
                        entries.add(new MutableShardRouting(shard));
                        if (shard.relocating()) {
                            entries = nodesToShards.get(shard.relocatingNodeId());
                            if (entries == null) {
                                entries = newArrayList();
                                nodesToShards.put(shard.relocatingNodeId(), entries);
                            }
                            
                            
                            entries.add(new MutableShardRouting(shard.index(), shard.id(), shard.relocatingNodeId(),
                                    shard.currentNodeId(), shard.primary(), ShardRoutingState.INITIALIZING, shard.version()));
                        }
                    } else {
                        unassigned.add(new MutableShardRouting(shard));
                    }
                }
            }
        }
        for (Map.Entry<String, List<MutableShardRouting>> entry : nodesToShards.entrySet()) {
            String nodeId = entry.getKey();
            this.nodesToShards.put(nodeId, new RoutingNode(nodeId, clusterState.nodes().get(nodeId), entry.getValue()));
        }
    }

    
    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<RoutingNode> iterator() {
        return nodesToShards.values().iterator();
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
     * Gets the routing table.
     *
     * @return the routing table
     */
    public RoutingTable getRoutingTable() {
        return routingTable();
    }

    
    /**
     * Meta data.
     *
     * @return the meta data
     */
    public MetaData metaData() {
        return this.metaData;
    }

    
    /**
     * Gets the meta data.
     *
     * @return the meta data
     */
    public MetaData getMetaData() {
        return metaData();
    }

    
    /**
     * Blocks.
     *
     * @return the cluster blocks
     */
    public ClusterBlocks blocks() {
        return this.blocks;
    }

    
    /**
     * Gets the blocks.
     *
     * @return the blocks
     */
    public ClusterBlocks getBlocks() {
        return this.blocks;
    }

    
    /**
     * Required average number of shards per node.
     *
     * @return the int
     */
    public int requiredAverageNumberOfShardsPerNode() {
        int totalNumberOfShards = 0;
        
        for (IndexMetaData indexMetaData : metaData.indices().values()) {
            if (indexMetaData.state() == IndexMetaData.State.OPEN) {
                totalNumberOfShards += indexMetaData.totalNumberOfShards();
            }
        }
        return totalNumberOfShards / nodesToShards.size();
    }

    
    /**
     * Checks for unassigned.
     *
     * @return true, if successful
     */
    public boolean hasUnassigned() {
        return !unassigned.isEmpty();
    }

    
    /**
     * Ignored unassigned.
     *
     * @return the list
     */
    public List<MutableShardRouting> ignoredUnassigned() {
        return this.ignoredUnassigned;
    }

    
    /**
     * Unassigned.
     *
     * @return the list
     */
    public List<MutableShardRouting> unassigned() {
        return this.unassigned;
    }

    
    /**
     * Gets the unassigned.
     *
     * @return the unassigned
     */
    public List<MutableShardRouting> getUnassigned() {
        return unassigned();
    }

    
    /**
     * Nodes to shards.
     *
     * @return the map
     */
    public Map<String, RoutingNode> nodesToShards() {
        return nodesToShards;
    }

    
    /**
     * Gets the nodes to shards.
     *
     * @return the nodes to shards
     */
    public Map<String, RoutingNode> getNodesToShards() {
        return nodesToShards();
    }

    
    /**
     * Node.
     *
     * @param nodeId the node id
     * @return the routing node
     */
    public RoutingNode node(String nodeId) {
        return nodesToShards.get(nodeId);
    }

    
    /**
     * Nodes per attributes counts.
     *
     * @param attributeName the attribute name
     * @return the t object int hash map
     */
    public TObjectIntHashMap<String> nodesPerAttributesCounts(String attributeName) {
        TObjectIntHashMap<String> nodesPerAttributesCounts = nodesPerAttributeNames.get(attributeName);
        if (nodesPerAttributesCounts != null) {
            return nodesPerAttributesCounts;
        }
        nodesPerAttributesCounts = new TObjectIntHashMap<String>();
        for (RoutingNode routingNode : this) {
            String attrValue = routingNode.node().attributes().get(attributeName);
            nodesPerAttributesCounts.adjustOrPutValue(attrValue, 1, 1);
        }
        nodesPerAttributeNames.put(attributeName, nodesPerAttributesCounts);
        return nodesPerAttributesCounts;
    }

    
    /**
     * Find primary for replica.
     *
     * @param shard the shard
     * @return the mutable shard routing
     */
    public MutableShardRouting findPrimaryForReplica(ShardRouting shard) {
        assert !shard.primary();
        for (RoutingNode routingNode : nodesToShards.values()) {
            List<MutableShardRouting> shards = routingNode.shards();
            for (int i = 0; i < shards.size(); i++) {
                MutableShardRouting shardRouting = shards.get(i);
                if (shardRouting.shardId().equals(shard.shardId()) && shardRouting.primary()) {
                    return shardRouting;
                }
            }
        }
        return null;
    }

    
    /**
     * Shards routing for.
     *
     * @param shardRouting the shard routing
     * @return the list
     */
    public List<MutableShardRouting> shardsRoutingFor(ShardRouting shardRouting) {
        return shardsRoutingFor(shardRouting.index(), shardRouting.id());
    }

    
    /**
     * Shards routing for.
     *
     * @param index the index
     * @param shardId the shard id
     * @return the list
     */
    public List<MutableShardRouting> shardsRoutingFor(String index, int shardId) {
        List<MutableShardRouting> shards = newArrayList();
        for (RoutingNode routingNode : this) {
            List<MutableShardRouting> nShards = routingNode.shards();
            for (int i = 0; i < nShards.size(); i++) {
                MutableShardRouting shardRouting = nShards.get(i);
                if (shardRouting.index().equals(index) && shardRouting.id() == shardId) {
                    shards.add(shardRouting);
                }
            }
        }
        for (int i = 0; i < unassigned.size(); i++) {
            MutableShardRouting shardRouting = unassigned.get(i);
            if (shardRouting.index().equals(index) && shardRouting.id() == shardId) {
                shards.add(shardRouting);
            }
        }
        return shards;
    }

    
    /**
     * Number of shards of type.
     *
     * @param state the state
     * @return the int
     */
    public int numberOfShardsOfType(ShardRoutingState state) {
        int count = 0;
        for (RoutingNode routingNode : this) {
            count += routingNode.numberOfShardsWithState(state);
        }
        return count;
    }

    
    /**
     * Shards with state.
     *
     * @param state the state
     * @return the list
     */
    public List<MutableShardRouting> shardsWithState(ShardRoutingState... state) {
        List<MutableShardRouting> shards = newArrayList();
        for (RoutingNode routingNode : this) {
            shards.addAll(routingNode.shardsWithState(state));
        }
        return shards;
    }

    
    /**
     * Shards with state.
     *
     * @param index the index
     * @param state the state
     * @return the list
     */
    public List<MutableShardRouting> shardsWithState(String index, ShardRoutingState... state) {
        List<MutableShardRouting> shards = newArrayList();
        for (RoutingNode routingNode : this) {
            shards.addAll(routingNode.shardsWithState(index, state));
        }
        return shards;
    }

    
    /**
     * Pretty print.
     *
     * @return the string
     */
    public String prettyPrint() {
        StringBuilder sb = new StringBuilder("routing_nodes:\n");
        for (RoutingNode routingNode : this) {
            sb.append(routingNode.prettyPrint());
        }
        sb.append("---- unassigned\n");
        for (MutableShardRouting shardEntry : unassigned) {
            sb.append("--------").append(shardEntry.shortSummary()).append('\n');
        }
        return sb.toString();
    }
}
