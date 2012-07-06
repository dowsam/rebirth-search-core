/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FlushNotAllowedEngineException.java 2012-3-29 15:02:43 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class FlushNotAllowedEngineException.
 *
 * @author l.xue.nong
 */
public class FlushNotAllowedEngineException extends EngineException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3902831913338137536L;

	
	/**
	 * Instantiates a new flush not allowed engine exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 */
	public FlushNotAllowedEngineException(ShardId shardId, String msg) {
		super(shardId, msg);
	}
}
