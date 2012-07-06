/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardRecoveringException.java 2012-3-29 15:01:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.shard;


/**
 * The Class IndexShardRecoveringException.
 *
 * @author l.xue.nong
 */
public class IndexShardRecoveringException extends IllegalIndexShardStateException {

    /**
     * Instantiates a new index shard recovering exception.
     *
     * @param shardId the shard id
     */
    public IndexShardRecoveringException(ShardId shardId) {
        super(shardId, IndexShardState.RECOVERING, "Already recovering");
    }
}
