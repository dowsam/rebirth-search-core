/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core EngineAlreadyStartedException.java 2012-3-29 15:01:20 l.xue.nong$$
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