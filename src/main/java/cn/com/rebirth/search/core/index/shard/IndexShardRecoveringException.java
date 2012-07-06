/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardRecoveringException.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard;

/**
 * The Class IndexShardRecoveringException.
 *
 * @author l.xue.nong
 */
public class IndexShardRecoveringException extends IllegalIndexShardStateException {

	/**
	 * Instantiates a new index shard recovering exception.
	 *
	 * @param shardId the shard id
	 */
	public IndexShardRecoveringException(ShardId shardId) {
		super(shardId, IndexShardState.RECOVERING, "Already recovering");
	}
}
