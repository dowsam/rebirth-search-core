/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core EngineCreationFailureException.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class EngineCreationFailureException.
 *
 * @author l.xue.nong
 */
public class EngineCreationFailureException extends EngineException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4487508250297563040L;

	/**
	 * Instantiates a new engine creation failure exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 * @param cause the cause
	 */
	public EngineCreationFailureException(ShardId shardId, String msg, Throwable cause) {
		super(shardId, msg, cause);
	}

}