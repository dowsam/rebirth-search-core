/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardNotRecoveringException.java 2012-7-6 14:30:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard;

/**
 * The Class IndexShardNotRecoveringException.
 *
 * @author l.xue.nong
 */
public class IndexShardNotRecoveringException extends IllegalIndexShardStateException {

	/**
	 * Instantiates a new index shard not recovering exception.
	 *
	 * @param shardId the shard id
	 * @param currentState the current state
	 */
	public IndexShardNotRecoveringException(ShardId shardId, IndexShardState currentState) {
		super(shardId, currentState, "Shard not in recovering state");
	}
}