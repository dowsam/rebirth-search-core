/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core OptimizeFailedEngineException.java 2012-7-6 14:30:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class OptimizeFailedEngineException.
 *
 * @author l.xue.nong
 */
public class OptimizeFailedEngineException extends EngineException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3838214698430812634L;

	/**
	 * Instantiates a new optimize failed engine exception.
	 *
	 * @param shardId the shard id
	 * @param t the t
	 */
	public OptimizeFailedEngineException(ShardId shardId, Throwable t) {
		super(shardId, "Optimize failed", t);
	}
}