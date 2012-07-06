/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core EngineAlreadyStartedException.java 2012-7-6 14:29:05 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class EngineAlreadyStartedException.
 *
 * @author l.xue.nong
 */
public class EngineAlreadyStartedException extends EngineException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6893619322695240672L;

	/**
	 * Instantiates a new engine already started exception.
	 *
	 * @param shardId the shard id
	 */
	public EngineAlreadyStartedException(ShardId shardId) {
		super(shardId, "Already started");
	}
}