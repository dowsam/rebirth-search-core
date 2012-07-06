/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RollbackNotAllowedEngineException.java 2012-3-29 15:02:27 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class RollbackNotAllowedEngineException.
 *
 * @author l.xue.nong
 */
public class RollbackNotAllowedEngineException extends EngineException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3541801898500237157L;

	
	/**
	 * Instantiates a new rollback not allowed engine exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 */
	public RollbackNotAllowedEngineException(ShardId shardId, String msg) {
		super(shardId, msg);
	}
}