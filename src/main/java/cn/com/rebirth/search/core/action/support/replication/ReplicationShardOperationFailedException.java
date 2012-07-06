/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ReplicationShardOperationFailedException.java 2012-3-29 15:00:44 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support.replication;

import cn.com.rebirth.commons.exception.RestartWrapperException;
import cn.com.rebirth.search.core.index.shard.IndexShardException;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class ReplicationShardOperationFailedException.
 *
 * @author l.xue.nong
 */
public class ReplicationShardOperationFailedException extends IndexShardException implements
		RestartWrapperException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2621932360190371894L;

	
	/**
	 * Instantiates a new replication shard operation failed exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 */
	public ReplicationShardOperationFailedException(ShardId shardId, String msg) {
		super(shardId, msg, null);
	}

	
	/**
	 * Instantiates a new replication shard operation failed exception.
	 *
	 * @param shardId the shard id
	 * @param cause the cause
	 */
	public ReplicationShardOperationFailedException(ShardId shardId, Throwable cause) {
		super(shardId, "", cause);
	}

	
	/**
	 * Instantiates a new replication shard operation failed exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 * @param cause the cause
	 */
	public ReplicationShardOperationFailedException(ShardId shardId, String msg, Throwable cause) {
		super(shardId, msg, cause);
	}
}