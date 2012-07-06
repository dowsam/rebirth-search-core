/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardRelocatedException.java 2012-7-6 14:29:00 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard;

/**
 * The Class IndexShardRelocatedException.
 *
 * @author l.xue.nong
 */
public class IndexShardRelocatedException extends IllegalIndexShardStateException {

	/**
	 * Instantiates a new index shard relocated exception.
	 *
	 * @param shardId the shard id
	 */
	public IndexShardRelocatedException(ShardId shardId) {
		super(shardId, IndexShardState.RELOCATED, "Already relocated");
	}
}