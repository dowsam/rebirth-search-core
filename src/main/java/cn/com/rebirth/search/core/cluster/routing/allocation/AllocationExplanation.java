/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AllocationExplanation.java 2012-3-29 15:02:52 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.allocation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.index.shard.ShardId;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * The Class AllocationExplanation.
 *
 * @author l.xue.nong
 */
public class AllocationExplanation implements Streamable {

    
    /** The Constant EMPTY. */
    public static final AllocationExplanation EMPTY = new AllocationExplanation();

    
    /**
     * The Class NodeExplanation.
     *
     * @author l.xue.nong
     */
    public static class NodeExplanation {
        
        
        /** The node. */
        private final DiscoveryNode node;

        
        /** The description. */
        private final String description;

        
        /**
         * Instantiates a new node explanation.
         *
         * @param node the node
         * @param description the description
         */
        public NodeExplanation(DiscoveryNode node, String description) {
            this.node = node;
            this.description = description;
        }

        
        /**
         * Node.
         *
         * @return the discovery node
         */
        public DiscoveryNode node() {
            return node;
        }

        
        /**
         * Description.
         *
         * @return the string
         */
        public String description() {
            return description;
        }
    }

    
    /** The explanations. */
    private final Map<ShardId, List<NodeExplanation>> explanations = Maps.newHashMap();

    
    /**
     * Adds the.
     *
     * @param shardId the shard id
     * @param nodeExplanation the node explanation
     * @return the allocation explanation
     */
    public AllocationExplanation add(ShardId shardId, NodeExplanation nodeExplanation) {
        List<NodeExplanation> list = explanations.get(shardId);
        if (list == null) {
            list = Lists.newArrayList();
            explanations.put(shardId, list);
        }
        list.add(nodeExplanation);
        return this;
    }

    
    /**
     * Explanations.
     *
     * @return the map
     */
    public Map<ShardId, List<NodeExplanation>> explanations() {
        return this.explanations;
    }

    
    /**
     * Read allocation explanation.
     *
     * @param in the in
     * @return the allocation explanation
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static AllocationExplanation readAllocationExplanation(StreamInput in) throws IOException {
        AllocationExplanation e = new AllocationExplanation();
        e.readFrom(in);
        return e;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        int size = in.readVInt();
        for (int i = 0; i < size; i++) {
            ShardId shardId = ShardId.readShardId(in);
            int size2 = in.readVInt();
            List<NodeExplanation> ne = Lists.newArrayListWithCapacity(size2);
            for (int j = 0; j < size2; j++) {
                DiscoveryNode node = null;
                if (in.readBoolean()) {
                    node = DiscoveryNode.readNode(in);
                }
                ne.add(new NodeExplanation(node, in.readUTF()));
            }
            explanations.put(shardId, ne);
        }
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeVInt(explanations.size());
        for (Map.Entry<ShardId, List<NodeExplanation>> entry : explanations.entrySet()) {
            entry.getKey().writeTo(out);
            out.writeVInt(entry.getValue().size());
            for (NodeExplanation nodeExplanation : entry.getValue()) {
                if (nodeExplanation.node() == null) {
                    out.writeBoolean(false);
                } else {
                    out.writeBoolean(true);
                    nodeExplanation.node().writeTo(out);
                }
                out.writeUTF(nodeExplanation.description());
            }
        }
    }
}
