/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardNotRecoveringException.java 2012-3-29 15:02:16 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.shard;


/**
 * The Class IndexShardNotRecoveringException.
 *
 * @author l.xue.nong
 */
public class IndexShardNotRecoveringException extends IllegalIndexShardStateException {

    /**
     * Instantiates a new index shard not recovering exception.
     *
     * @param shardId the shard id
     * @param currentState the current state
     */
    public IndexShardNotRecoveringException(ShardId shardId, IndexShardState currentState) {
        super(shardId, currentState, "Shard not in recovering state");
    }
}