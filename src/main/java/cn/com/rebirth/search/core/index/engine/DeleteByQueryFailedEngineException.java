/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DeleteByQueryFailedEngineException.java 2012-3-29 15:02:11 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class DeleteByQueryFailedEngineException.
 *
 * @author l.xue.nong
 */
public class DeleteByQueryFailedEngineException extends EngineException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5640327798855016228L;

	
	/**
	 * Instantiates a new delete by query failed engine exception.
	 *
	 * @param shardId the shard id
	 * @param deleteByQuery the delete by query
	 * @param cause the cause
	 */
	public DeleteByQueryFailedEngineException(ShardId shardId, Engine.DeleteByQuery deleteByQuery, Throwable cause) {
		super(shardId, "Delete by query failed for [" + deleteByQuery.query() + "]", cause);
	}
}