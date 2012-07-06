/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IllegalShardRoutingStateException.java 2012-7-6 14:30:24 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

/**
 * The Class IllegalShardRoutingStateException.
 *
 * @author l.xue.nong
 */
public class IllegalShardRoutingStateException extends RoutingException {

	/** The shard. */
	private final ShardRouting shard;

	/**
	 * Instantiates a new illegal shard routing state exception.
	 *
	 * @param shard the shard
	 * @param message the message
	 */
	public IllegalShardRoutingStateException(ShardRouting shard, String message) {
		this(shard, message, null);
	}

	/**
	 * Instantiates a new illegal shard routing state exception.
	 *
	 * @param shard the shard
	 * @param message the message
	 * @param cause the cause
	 */
	public IllegalShardRoutingStateException(ShardRouting shard, String message, Throwable cause) {
		super(shard.shortSummary() + ": " + message, cause);
		this.shard = shard;
	}

	/**
	 * Shard.
	 *
	 * @return the shard routing
	 */
	public ShardRouting shard() {
		return shard;
	}
}
