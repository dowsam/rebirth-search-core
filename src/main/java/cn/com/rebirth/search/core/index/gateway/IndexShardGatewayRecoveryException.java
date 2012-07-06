/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardGatewayRecoveryException.java 2012-3-29 15:02:36 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.gateway;

import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class IndexShardGatewayRecoveryException.
 *
 * @author l.xue.nong
 */
public class IndexShardGatewayRecoveryException extends IndexShardGatewayException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4108481701459325495L;

	
	/**
	 * Instantiates a new index shard gateway recovery exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 */
	public IndexShardGatewayRecoveryException(ShardId shardId, String msg) {
		super(shardId, msg);
	}

	
	/**
	 * Instantiates a new index shard gateway recovery exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 * @param cause the cause
	 */
	public IndexShardGatewayRecoveryException(ShardId shardId, String msg, Throwable cause) {
		super(shardId, msg, cause);
	}

}
