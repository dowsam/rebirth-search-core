/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchException.java 2012-7-6 14:29:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class SearchException.
 *
 * @author l.xue.nong
 */
public class SearchException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1540591423548842950L;

	/** The shard target. */
	private final SearchShardTarget shardTarget;

	/**
	 * Instantiates a new search exception.
	 *
	 * @param shardTarget the shard target
	 * @param msg the msg
	 */
	public SearchException(SearchShardTarget shardTarget, String msg) {
		super(msg);
		this.shardTarget = shardTarget;
	}

	/**
	 * Instantiates a new search exception.
	 *
	 * @param shardTarget the shard target
	 * @param msg the msg
	 * @param cause the cause
	 */
	public SearchException(SearchShardTarget shardTarget, String msg, Throwable cause) {
		super(msg, cause);
		this.shardTarget = shardTarget;
	}

	/**
	 * Shard.
	 *
	 * @return the search shard target
	 */
	public SearchShardTarget shard() {
		return this.shardTarget;
	}
}
