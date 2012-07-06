/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterChangedEvent.java 2012-3-29 15:00:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster;

import java.util.List;

import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;


/**
 * The Class ClusterChangedEvent.
 *
 * @author l.xue.nong
 */
public class ClusterChangedEvent {

    
    /** The source. */
    private final String source;

    
    /** The previous state. */
    private final ClusterState previousState;

    
    /** The state. */
    private final ClusterState state;

    
    /** The nodes delta. */
    private final DiscoveryNodes.Delta nodesDelta;

    
    /**
     * Instantiates a new cluster changed event.
     *
     * @param source the source
     * @param state the state
     * @param previousState the previous state
     */
    public ClusterChangedEvent(String source, ClusterState state, ClusterState previousState) {
        this.source = source;
        this.state = state;
        this.previousState = previousState;
        this.nodesDelta = state.nodes().delta(previousState.nodes());
    }

    
    /**
     * Source.
     *
     * @return the string
     */
    public String source() {
        return this.source;
    }

    
    /**
     * State.
     *
     * @return the cluster state
     */
    public ClusterState state() {
        return this.state;
    }

    
    /**
     * Previous state.
     *
     * @return the cluster state
     */
    public ClusterState previousState() {
        return this.previousState;
    }

    
    /**
     * Routing table changed.
     *
     * @return true, if successful
     */
    public boolean routingTableChanged() {
        return state.routingTable() != previousState.routingTable();
    }

    
    /**
     * Index routing table changed.
     *
     * @param index the index
     * @return true, if successful
     */
    public boolean indexRoutingTableChanged(String index) {
        if (!state.routingTable().hasIndex(index) && !previousState.routingTable().hasIndex(index)) {
            return false;
        }
        if (state.routingTable().hasIndex(index) && previousState.routingTable().hasIndex(index)) {
            return state.routingTable().index(index) != previousState.routingTable().index(index);
        }
        return true;
    }

    
    /**
     * Indices created.
     *
     * @return the list
     */
    public List<String> indicesCreated() {
        if (previousState == null) {
            return Lists.newArrayList(state.metaData().indices().keySet());
        }
        if (!metaDataChanged()) {
            return ImmutableList.of();
        }
        List<String> created = null;
        for (String index : state.metaData().indices().keySet()) {
            if (!previousState.metaData().hasIndex(index)) {
                if (created == null) {
                    created = Lists.newArrayList();
                }
                created.add(index);
            }
        }
        return created == null ? ImmutableList.<String>of() : created;
    }

    
    /**
     * Indices deleted.
     *
     * @return the list
     */
    public List<String> indicesDeleted() {
        if (previousState == null) {
            return ImmutableList.of();
        }
        if (!metaDataChanged()) {
            return ImmutableList.of();
        }
        List<String> deleted = null;
        for (String index : previousState.metaData().indices().keySet()) {
            if (!state.metaData().hasIndex(index)) {
                if (deleted == null) {
                    deleted = Lists.newArrayList();
                }
                deleted.add(index);
            }
        }
        return deleted == null ? ImmutableList.<String>of() : deleted;
    }

    
    /**
     * Meta data changed.
     *
     * @return true, if successful
     */
    public boolean metaDataChanged() {
        return state.metaData() != previousState.metaData();
    }

    
    /**
     * Index meta data changed.
     *
     * @param current the current
     * @return true, if successful
     */
    public boolean indexMetaDataChanged(IndexMetaData current) {
        MetaData previousMetaData = previousState.metaData();
        if (previousMetaData == null) {
            return true;
        }
        IndexMetaData previousIndexMetaData = previousMetaData.index(current.index());
        
        
        if (previousIndexMetaData == current) {
            return false;
        }
        return true;
    }

    
    /**
     * Blocks changed.
     *
     * @return true, if successful
     */
    public boolean blocksChanged() {
        return state.blocks() != previousState.blocks();
    }

    
    /**
     * Local node master.
     *
     * @return true, if successful
     */
    public boolean localNodeMaster() {
        return state.nodes().localNodeMaster();
    }

    
    /**
     * Nodes delta.
     *
     * @return the discovery nodes. delta
     */
    public DiscoveryNodes.Delta nodesDelta() {
        return this.nodesDelta;
    }

    
    /**
     * Nodes removed.
     *
     * @return true, if successful
     */
    public boolean nodesRemoved() {
        return nodesDelta.removed();
    }

    
    /**
     * Nodes added.
     *
     * @return true, if successful
     */
    public boolean nodesAdded() {
        return nodesDelta.added();
    }

    
    /**
     * Nodes changed.
     *
     * @return true, if successful
     */
    public boolean nodesChanged() {
        return nodesRemoved() || nodesAdded();
    }
}
