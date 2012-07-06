/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardStats.java 2012-3-29 15:01:43 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.stats;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationResponse;
import cn.com.rebirth.search.core.cluster.routing.ImmutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;


/**
 * The Class ShardStats.
 *
 * @author l.xue.nong
 */
public class ShardStats extends BroadcastShardOperationResponse {

    
    /** The shard routing. */
    private ShardRouting shardRouting;

    
    /** The stats. */
    CommonStats stats;

    
    /**
     * Instantiates a new shard stats.
     */
    ShardStats() {
    }

    
    /**
     * Instantiates a new shard stats.
     *
     * @param shardRouting the shard routing
     */
    ShardStats(ShardRouting shardRouting) {
        super(shardRouting.index(), shardRouting.id());
        this.shardRouting = shardRouting;
        this.stats = new CommonStats();
    }

    
    /**
     * Shard routing.
     *
     * @return the shard routing
     */
    public ShardRouting shardRouting() {
        return this.shardRouting;
    }

    
    /**
     * Gets the shard routing.
     *
     * @return the shard routing
     */
    public ShardRouting getShardRouting() {
        return shardRouting();
    }

    
    /**
     * Stats.
     *
     * @return the common stats
     */
    public CommonStats stats() {
        return this.stats;
    }

    
    /**
     * Gets the stats.
     *
     * @return the stats
     */
    public CommonStats getStats() {
        return stats();
    }

    
    /**
     * Read shard stats.
     *
     * @param in the in
     * @return the shard stats
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static ShardStats readShardStats(StreamInput in) throws IOException {
        ShardStats stats = new ShardStats();
        stats.readFrom(in);
        return stats;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationResponse#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        shardRouting = ImmutableShardRouting.readShardRoutingEntry(in);
        stats = CommonStats.readCommonStats(in);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationResponse#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        shardRouting.writeTo(out);
        stats.writeTo(out);
    }
}
