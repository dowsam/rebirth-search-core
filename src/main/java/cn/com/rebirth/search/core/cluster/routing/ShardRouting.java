/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardRouting.java 2012-3-29 15:02:17 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing;

import java.io.IOException;
import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Interface ShardRouting.
 *
 * @author l.xue.nong
 */
public interface ShardRouting extends Streamable, Serializable {

    
    /**
     * Shard id.
     *
     * @return the shard id
     */
    ShardId shardId();

    
    /**
     * Index.
     *
     * @return the string
     */
    String index();

    
    /**
     * Gets the index.
     *
     * @return the index
     */
    String getIndex();

    
    /**
     * Id.
     *
     * @return the int
     */
    int id();

    
    /**
     * Gets the id.
     *
     * @return the id
     */
    int getId();

    
    /**
     * Version.
     *
     * @return the long
     */
    long version();

    
    /**
     * State.
     *
     * @return the shard routing state
     */
    ShardRoutingState state();

    
    /**
     * Unassigned.
     *
     * @return true, if successful
     */
    boolean unassigned();

    
    /**
     * Initializing.
     *
     * @return true, if successful
     */
    boolean initializing();

    
    /**
     * Started.
     *
     * @return true, if successful
     */
    boolean started();

    
    /**
     * Relocating.
     *
     * @return true, if successful
     */
    boolean relocating();

    
    /**
     * Active.
     *
     * @return true, if successful
     */
    boolean active();

    
    /**
     * Assigned to node.
     *
     * @return true, if successful
     */
    boolean assignedToNode();

    
    /**
     * Current node id.
     *
     * @return the string
     */
    String currentNodeId();

    
    /**
     * Relocating node id.
     *
     * @return the string
     */
    String relocatingNodeId();

    
    /**
     * Primary.
     *
     * @return true, if successful
     */
    boolean primary();

    
    /**
     * Short summary.
     *
     * @return the string
     */
    String shortSummary();

    
    /**
     * Shards it.
     *
     * @return the shard iterator
     */
    ShardIterator shardsIt();

    
    /**
     * Write to thin.
     *
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void writeToThin(StreamOutput out) throws IOException;

    
    /**
     * Read from thin.
     *
     * @param in the in
     * @throws ClassNotFoundException the class not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void readFromThin(StreamInput in) throws ClassNotFoundException, IOException;
}
