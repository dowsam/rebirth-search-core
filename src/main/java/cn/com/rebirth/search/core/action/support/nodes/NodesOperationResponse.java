/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodesOperationResponse.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support.nodes;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.cluster.ClusterName;

import com.google.common.collect.Maps;


/**
 * The Class NodesOperationResponse.
 *
 * @param <NodeResponse> the generic type
 * @author l.xue.nong
 */
public abstract class NodesOperationResponse<NodeResponse extends NodeOperationResponse> implements ActionResponse, Iterable<NodeResponse> {

    
    /** The cluster name. */
    private ClusterName clusterName;

    
    /** The nodes. */
    protected NodeResponse[] nodes;

    
    /** The nodes map. */
    private Map<String, NodeResponse> nodesMap;

    
    /**
     * Instantiates a new nodes operation response.
     */
    protected NodesOperationResponse() {
    }

    
    /**
     * Instantiates a new nodes operation response.
     *
     * @param clusterName the cluster name
     * @param nodes the nodes
     */
    protected NodesOperationResponse(ClusterName clusterName, NodeResponse[] nodes) {
        this.clusterName = clusterName;
        this.nodes = nodes;
    }

    
    /**
     * Cluster name.
     *
     * @return the cluster name
     */
    public ClusterName clusterName() {
        return this.clusterName;
    }

    
    /**
     * Gets the cluster name.
     *
     * @return the cluster name
     */
    public String getClusterName() {
        return clusterName().value();
    }

    
    /**
     * Nodes.
     *
     * @return the node response[]
     */
    public NodeResponse[] nodes() {
        return nodes;
    }

    
    /**
     * Gets the nodes.
     *
     * @return the nodes
     */
    public NodeResponse[] getNodes() {
        return nodes();
    }

    
    /**
     * Gets the at.
     *
     * @param position the position
     * @return the at
     */
    public NodeResponse getAt(int position) {
        return nodes[position];
    }

    
    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<NodeResponse> iterator() {
        return nodesMap().values().iterator();
    }

    
    /**
     * Nodes map.
     *
     * @return the map
     */
    public Map<String, NodeResponse> nodesMap() {
        if (nodesMap == null) {
            nodesMap = Maps.newHashMap();
            for (NodeResponse nodeResponse : nodes) {
                nodesMap.put(nodeResponse.node().id(), nodeResponse);
            }
        }
        return nodesMap;
    }

    
    /**
     * Gets the nodes map.
     *
     * @return the nodes map
     */
    public Map<String, NodeResponse> getNodesMap() {
        return nodesMap();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        clusterName = ClusterName.readClusterName(in);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        clusterName.writeTo(out);
    }
}
