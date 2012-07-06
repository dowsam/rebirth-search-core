/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardStartedException.java 2012-7-6 14:29:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard;

/**
 * The Class IndexShardStartedException.
 *
 * @author l.xue.nong
 */
public class IndexShardStartedException extends IllegalIndexShardStateException {

	/**
	 * Instantiates a new index shard started exception.
	 *
	 * @param shardId the shard id
	 */
	public IndexShardStartedException(ShardId shardId) {
		super(shardId, IndexShardState.STARTED, "Already started");
	}
}
