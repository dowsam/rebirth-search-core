/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IgnoreGatewayRecoveryException.java 2012-7-6 14:28:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.gateway;

import cn.com.rebirth.search.core.index.shard.IndexShardException;
import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class IgnoreGatewayRecoveryException.
 *
 * @author l.xue.nong
 */
public class IgnoreGatewayRecoveryException extends IndexShardException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5947369619930851788L;

	/**
	 * Instantiates a new ignore gateway recovery exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 */
	public IgnoreGatewayRecoveryException(ShardId shardId, String msg) {
		super(shardId, msg);
	}

	/**
	 * Instantiates a new ignore gateway recovery exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 * @param cause the cause
	 */
	public IgnoreGatewayRecoveryException(ShardId shardId, String msg, Throwable cause) {
		super(shardId, msg, cause);
	}
}
