/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardOperationFailedException.java 2012-7-6 14:28:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.Streamable;

/**
 * The Interface ShardOperationFailedException.
 *
 * @author l.xue.nong
 */
public interface ShardOperationFailedException extends Streamable, Serializable {

	/**
	 * Index.
	 *
	 * @return the string
	 */
	String index();

	/**
	 * Shard id.
	 *
	 * @return the int
	 */
	int shardId();

	/**
	 * Reason.
	 *
	 * @return the string
	 */
	String reason();
}
