/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RollbackFailedEngineException.java 2012-7-6 14:29:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class RollbackFailedEngineException.
 *
 * @author l.xue.nong
 */
public class RollbackFailedEngineException extends EngineException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6081480173081983477L;

	/**
	 * Instantiates a new rollback failed engine exception.
	 *
	 * @param shardId the shard id
	 * @param t the t
	 */
	public RollbackFailedEngineException(ShardId shardId, Throwable t) {
		super(shardId, "Rollback failed", t);
	}
}