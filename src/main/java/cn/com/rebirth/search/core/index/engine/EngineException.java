/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core EngineException.java 2012-7-6 14:29:49 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.IndexShardException;
import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class EngineException.
 *
 * @author l.xue.nong
 */
public class EngineException extends IndexShardException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -134041529051357353L;

	/**
	 * Instantiates a new engine exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 */
	public EngineException(ShardId shardId, String msg) {
		super(shardId, msg);
	}

	/**
	 * Instantiates a new engine exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 * @param cause the cause
	 */
	public EngineException(ShardId shardId, String msg, Throwable cause) {
		super(shardId, msg, cause);
	}
}