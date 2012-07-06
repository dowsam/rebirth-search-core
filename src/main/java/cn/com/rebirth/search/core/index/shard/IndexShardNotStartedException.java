/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardNotStartedException.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard;

/**
 * The Class IndexShardNotStartedException.
 *
 * @author l.xue.nong
 */
public class IndexShardNotStartedException extends IllegalIndexShardStateException {

	/**
	 * Instantiates a new index shard not started exception.
	 *
	 * @param shardId the shard id
	 * @param currentState the current state
	 */
	public IndexShardNotStartedException(ShardId shardId, IndexShardState currentState) {
		super(shardId, currentState, "Shard not started");
	}
}