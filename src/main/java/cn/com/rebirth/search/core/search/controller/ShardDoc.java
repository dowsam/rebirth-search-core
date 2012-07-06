/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardDoc.java 2012-3-29 15:01:08 l.xue.nong$$
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
