/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StoreException.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store;

import cn.com.rebirth.search.core.index.shard.IndexShardException;
import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class StoreException.
 *
 * @author l.xue.nong
 */
public class StoreException extends IndexShardException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3934413518755004717L;

	/**
	 * Instantiates a new store exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 */
	public StoreException(ShardId shardId, String msg) {
		super(shardId, msg);
	}

	/**
	 * Instantiates a new store exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 * @param cause the cause
	 */
	public StoreException(ShardId shardId, String msg, Throwable cause) {
		super(shardId, msg, cause);
	}
}