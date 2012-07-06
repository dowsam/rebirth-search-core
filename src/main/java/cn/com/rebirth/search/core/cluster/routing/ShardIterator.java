/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardIterator.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Interface ShardIterator.
 *
 * @author l.xue.nong
 */
public interface ShardIterator extends ShardsIterator {

	/**
	 * Shard id.
	 *
	 * @return the shard id
	 */
	ShardId shardId();

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.ShardsIterator#reset()
	 */
	ShardIterator reset();
}
