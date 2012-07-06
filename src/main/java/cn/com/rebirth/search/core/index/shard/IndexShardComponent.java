/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardComponent.java 2012-7-6 14:29:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard;

import cn.com.rebirth.commons.settings.Settings;

/**
 * The Interface IndexShardComponent.
 *
 * @author l.xue.nong
 */
public interface IndexShardComponent {

	/**
	 * Shard id.
	 *
	 * @return the shard id
	 */
	ShardId shardId();

	/**
	 * Index settings.
	 *
	 * @return the settings
	 */
	Settings indexSettings();
}
