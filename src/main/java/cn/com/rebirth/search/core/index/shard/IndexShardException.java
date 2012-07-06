/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardException.java 2012-3-29 15:01:20 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.shard;

import cn.com.rebirth.search.core.index.IndexException;


/**
 * The Class IndexShardException.
 *
 * @author l.xue.nong
 */
public class IndexShardException extends IndexException {

    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6106338182243518781L;
	
	
    /** The shard id. */
    private final ShardId shardId;

    
    /**
     * Instantiates a new index shard exception.
     *
     * @param shardId the shard id
     * @param msg the msg
     */
    public IndexShardException(ShardId shardId, String msg) {
        this(shardId, msg, null);
    }

    
    /**
     * Instantiates a new index shard exception.
     *
     * @param shardId the shard id
     * @param msg the msg
     * @param cause the cause
     */
    public IndexShardException(ShardId shardId, String msg, Throwable cause) {
        super(shardId == null ? null : shardId.index(), false, "[" + (shardId == null ? "_na" : shardId.id()) + "] " + msg, cause);
        this.shardId = shardId;
    }

    
    /**
     * Shard id.
     *
     * @return the shard id
     */
    public ShardId shardId() {
        return shardId;
    }
}
