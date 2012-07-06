/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardNotStartedException.java 2012-3-29 15:02:09 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.shard;


/**
 * The Class IndexShardNotStartedException.
 *
 * @author l.xue.nong
 */
public class IndexShardNotStartedException extends IllegalIndexShardStateException {

    /**
     * Instantiates a new index shard not started exception.
     *
     * @param shardId the shard id
     * @param currentState the current state
     */
    public IndexShardNotStartedException(ShardId shardId, IndexShardState currentState) {
        super(shardId, currentState, "Shard not started");
    }
}