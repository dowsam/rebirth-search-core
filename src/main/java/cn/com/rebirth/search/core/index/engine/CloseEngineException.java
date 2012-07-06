/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CloseEngineException.java 2012-7-6 14:29:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class CloseEngineException.
 *
 * @author l.xue.nong
 */
public class CloseEngineException extends EngineException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2036524683503243449L;

	/**
	 * Instantiates a new close engine exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 */
	public CloseEngineException(ShardId shardId, String msg) {
		super(shardId, msg);
	}

	/**
	 * Instantiates a new close engine exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 * @param cause the cause
	 */
	public CloseEngineException(ShardId shardId, String msg, Throwable cause) {
		super(shardId, msg, cause);
	}
}