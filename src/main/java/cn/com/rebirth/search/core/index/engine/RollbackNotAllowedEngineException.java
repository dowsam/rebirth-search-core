/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RollbackNotAllowedEngineException.java 2012-7-6 14:29:25 l.xue.nong$$
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