/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NoShardAvailableActionException.java 2012-3-29 15:02:25 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;

import cn.com.rebirth.search.core.index.shard.IndexShardException;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class NoShardAvailableActionException.
 *
 * @author l.xue.nong
 */
public class NoShardAvailableActionException extends IndexShardException {
	
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3955723066804727887L;

	
	/**
	 * Instantiates a new no shard available action exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 */
	public NoShardAvailableActionException(ShardId shardId, String msg) {
		super(shardId, msg);
	}

	
	/**
	 * Instantiates a new no shard available action exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 * @param cause the cause
	 */
	public NoShardAvailableActionException(ShardId shardId, String msg, Throwable cause) {
		super(shardId, msg, cause);
	}

}