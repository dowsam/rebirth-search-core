/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardComponent.java 2012-3-29 15:01:17 l.xue.nong$$
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
