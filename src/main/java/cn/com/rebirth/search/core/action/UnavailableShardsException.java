/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core UnavailableShardsException.java 2012-3-29 15:02:05 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class UnavailableShardsException.
 *
 * @author l.xue.nong
 */
public class UnavailableShardsException extends RestartException {

    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5694902202413101334L;

	
	/**
	 * Instantiates a new unavailable shards exception.
	 *
	 * @param shardId the shard id
	 * @param message the message
	 */
	public UnavailableShardsException(@Nullable ShardId shardId, String message) {
        super(buildMessage(shardId, message));
    }

    
    /**
     * Builds the message.
     *
     * @param shardId the shard id
     * @param message the message
     * @return the string
     */
    private static String buildMessage(ShardId shardId, String message) {
        if (shardId == null) {
            return message;
        }
        return "[" + shardId.index().name() + "][" + shardId.id() + "] " + message;
    }

}