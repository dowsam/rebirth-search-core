/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardDoc.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.controller;

import cn.com.rebirth.search.core.search.SearchShardTarget;

/**
 * The Interface ShardDoc.
 *
 * @author l.xue.nong
 */
public interface ShardDoc {

	/**
	 * Shard target.
	 *
	 * @return the search shard target
	 */
	SearchShardTarget shardTarget();

	/**
	 * Doc id.
	 *
	 * @return the int
	 */
	int docId();

	/**
	 * Score.
	 *
	 * @return the float
	 */
	float score();
}
