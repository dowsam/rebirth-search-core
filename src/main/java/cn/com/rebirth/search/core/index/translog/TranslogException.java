/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TranslogException.java 2012-7-6 14:29:02 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.translog;

import cn.com.rebirth.search.core.index.shard.IndexShardException;
import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class TranslogException.
 *
 * @author l.xue.nong
 */
public class TranslogException extends IndexShardException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7853995853779128299L;

	/**
	 * Instantiates a new translog exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 */
	public TranslogException(ShardId shardId, String msg) {
		super(shardId, msg);
	}

	/**
	 * Instantiates a new translog exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 * @param cause the cause
	 */
	public TranslogException(ShardId shardId, String msg, Throwable cause) {
		super(shardId, msg, cause);
	}
}