/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardCreationException.java 2012-7-6 14:29:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard;

/**
 * The Class IndexShardCreationException.
 *
 * @author l.xue.nong
 */
public class IndexShardCreationException extends IndexShardException {

	/**
	 * Instantiates a new index shard creation exception.
	 *
	 * @param shardId the shard id
	 * @param cause the cause
	 */
	public IndexShardCreationException(ShardId shardId, Throwable cause) {
		super(shardId, "failed to create shard", cause);
	}
}
