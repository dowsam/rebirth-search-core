/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RefreshFailedEngineException.java 2012-7-6 14:30:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class RefreshFailedEngineException.
 *
 * @author l.xue.nong
 */
public class RefreshFailedEngineException extends EngineException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6670761740255024093L;

	/**
	 * Instantiates a new refresh failed engine exception.
	 *
	 * @param shardId the shard id
	 * @param t the t
	 */
	public RefreshFailedEngineException(ShardId shardId, Throwable t) {
		super(shardId, "Refresh failed", t);
	}
}