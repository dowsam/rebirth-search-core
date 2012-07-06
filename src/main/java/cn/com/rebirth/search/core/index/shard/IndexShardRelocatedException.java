/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardRelocatedException.java 2012-3-29 15:00:46 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.shard;


/**
 * The Class IndexShardRelocatedException.
 *
 * @author l.xue.nong
 */
public class IndexShardRelocatedException extends IllegalIndexShardStateException {

    /**
     * Instantiates a new index shard relocated exception.
     *
     * @param shardId the shard id
     */
    public IndexShardRelocatedException(ShardId shardId) {
        super(shardId, IndexShardState.RELOCATED, "Already relocated");
    }
}