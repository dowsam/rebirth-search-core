/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IllegalShardRoutingStateException.java 2012-3-29 15:01:47 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing;


/**
 * The Class IllegalShardRoutingStateException.
 *
 * @author l.xue.nong
 */
public class IllegalShardRoutingStateException extends RoutingException {

    /** The shard. */
    private final ShardRouting shard;

    /**
     * Instantiates a new illegal shard routing state exception.
     *
     * @param shard the shard
     * @param message the message
     */
    public IllegalShardRoutingStateException(ShardRouting shard, String message) {
        this(shard, message, null);
    }

    /**
     * Instantiates a new illegal shard routing state exception.
     *
     * @param shard the shard
     * @param message the message
     * @param cause the cause
     */
    public IllegalShardRoutingStateException(ShardRouting shard, String message, Throwable cause) {
        super(shard.shortSummary() + ": " + message, cause);
        this.shard = shard;
    }

    /**
     * Shard.
     *
     * @return the shard routing
     */
    public ShardRouting shard() {
        return shard;
    }
}
