/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardStartedException.java 2012-3-29 15:01:47 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.shard;


/**
 * The Class IndexShardStartedException.
 *
 * @author l.xue.nong
 */
public class IndexShardStartedException extends IllegalIndexShardStateException {

    /**
     * Instantiates a new index shard started exception.
     *
     * @param shardId the shard id
     */
    public IndexShardStartedException(ShardId shardId) {
        super(shardId, IndexShardState.STARTED, "Already started");
    }
}
