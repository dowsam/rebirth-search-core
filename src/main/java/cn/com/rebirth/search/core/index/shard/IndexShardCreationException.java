/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardCreationException.java 2012-3-29 15:01:30 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.shard;


/**
 * The Class IndexShardCreationException.
 *
 * @author l.xue.nong
 */
public class IndexShardCreationException extends IndexShardException {

    /**
     * Instantiates a new index shard creation exception.
     *
     * @param shardId the shard id
     * @param cause the cause
     */
    public IndexShardCreationException(ShardId shardId, Throwable cause) {
        super(shardId, "failed to create shard", cause);
    }
}
