/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchPhaseResult.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import cn.com.rebirth.commons.io.stream.Streamable;

/**
 * The Interface SearchPhaseResult.
 *
 * @author l.xue.nong
 */
public interface SearchPhaseResult extends Streamable {

	/**
	 * Id.
	 *
	 * @return the long
	 */
	long id();

	/**
	 * Shard target.
	 *
	 * @return the search shard target
	 */
	SearchShardTarget shardTarget();

	/**
	 * Shard target.
	 *
	 * @param shardTarget the shard target
	 */
	void shardTarget(SearchShardTarget shardTarget);
}
