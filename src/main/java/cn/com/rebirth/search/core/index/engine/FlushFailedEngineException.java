/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FlushFailedEngineException.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class FlushFailedEngineException.
 *
 * @author l.xue.nong
 */
public class FlushFailedEngineException extends EngineException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2365842014907385536L;

	/**
	 * Instantiates a new flush failed engine exception.
	 *
	 * @param shardId the shard id
	 * @param t the t
	 */
	public FlushFailedEngineException(ShardId shardId, Throwable t) {
		super(shardId, "Flush failed", t);
	}

	/**
	 * Instantiates a new flush failed engine exception.
	 *
	 * @param shardId the shard id
	 * @param message the message
	 * @param t the t
	 */
	public FlushFailedEngineException(ShardId shardId, String message, Throwable t) {
		super(shardId, "Flush failed [" + message + "]", t);
	}
}