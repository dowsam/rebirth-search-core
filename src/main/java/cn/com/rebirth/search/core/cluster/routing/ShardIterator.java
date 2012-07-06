/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardIterator.java 2012-3-29 15:01:53 l.xue.nong$$
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
	 * @see cn.com.summall.search.core.cluster.routing.ShardsIterator#reset()
	 */
	ShardIterator reset();
}
