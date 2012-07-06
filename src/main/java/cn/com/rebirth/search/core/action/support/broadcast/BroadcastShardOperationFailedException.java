/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BroadcastShardOperationFailedException.java 2012-3-29 15:02:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support.broadcast;

import cn.com.rebirth.commons.exception.RestartWrapperException;
import cn.com.rebirth.search.core.index.shard.IndexShardException;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class BroadcastShardOperationFailedException.
 *
 * @author l.xue.nong
 */
public class BroadcastShardOperationFailedException extends IndexShardException implements RestartWrapperException {

    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3010181777731283546L;

	
    /**
     * Instantiates a new broadcast shard operation failed exception.
     *
     * @param shardId the shard id
     * @param msg the msg
     */
    public BroadcastShardOperationFailedException(ShardId shardId, String msg) {
        super(shardId, msg, null);
    }

    
    /**
     * Instantiates a new broadcast shard operation failed exception.
     *
     * @param shardId the shard id
     * @param cause the cause
     */
    public BroadcastShardOperationFailedException(ShardId shardId, Throwable cause) {
        super(shardId, "", cause);
    }

    
    /**
     * Instantiates a new broadcast shard operation failed exception.
     *
     * @param shardId the shard id
     * @param msg the msg
     * @param cause the cause
     */
    public BroadcastShardOperationFailedException(ShardId shardId, String msg, Throwable cause) {
        super(shardId, msg, cause);
    }
}