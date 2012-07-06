/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core EngineClosedException.java 2012-3-29 15:02:48 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.IndexShardClosedException;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class EngineClosedException.
 *
 * @author l.xue.nong
 */
public class EngineClosedException extends IndexShardClosedException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6472052088856648295L;

	
	/**
	 * Instantiates a new engine closed exception.
	 *
	 * @param shardId the shard id
	 */
	public EngineClosedException(ShardId shardId) {
		super(shardId);
	}

	
	/**
	 * Instantiates a new engine closed exception.
	 *
	 * @param shardId the shard id
	 * @param t the t
	 */
	public EngineClosedException(ShardId shardId, Throwable t) {
		super(shardId, t);
	}
}
