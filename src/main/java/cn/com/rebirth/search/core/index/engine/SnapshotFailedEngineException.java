/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SnapshotFailedEngineException.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class SnapshotFailedEngineException.
 *
 * @author l.xue.nong
 */
public class SnapshotFailedEngineException extends EngineException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8938882023105302407L;

	/**
	 * Instantiates a new snapshot failed engine exception.
	 *
	 * @param shardId the shard id
	 * @param cause the cause
	 */
	public SnapshotFailedEngineException(ShardId shardId, Throwable cause) {
		super(shardId, "Snapshot failed", cause);
	}

}