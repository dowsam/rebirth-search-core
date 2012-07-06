/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardStateInfo.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway.local.state.shards;

import cn.com.rebirth.commons.Nullable;

/**
 * The Class ShardStateInfo.
 *
 * @author l.xue.nong
 */
public class ShardStateInfo {

	/** The version. */
	public final long version;

	/** The primary. */
	@Nullable
	public final Boolean primary;

	/**
	 * Instantiates a new shard state info.
	 *
	 * @param version the version
	 * @param primary the primary
	 */
	public ShardStateInfo(long version, Boolean primary) {
		this.version = version;
		this.primary = primary;
	}
}
