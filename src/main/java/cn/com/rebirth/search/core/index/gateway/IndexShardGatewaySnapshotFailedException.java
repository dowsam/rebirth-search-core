/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardGatewaySnapshotFailedException.java 2012-7-6 14:29:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.gateway;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class IndexShardGatewaySnapshotFailedException.
 *
 * @author l.xue.nong
 */
public class IndexShardGatewaySnapshotFailedException extends IndexShardGatewayException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1322549411599008677L;

	/**
	 * Instantiates a new index shard gateway snapshot failed exception.
	 *
	 * @param shardId the shard id
	 * @param msg the msg
	 * @param cause the cause
	 */
	public IndexShardGatewaySnapshotFailedException(ShardId shardId, String msg, Throwable cause) {
		super(shardId, msg, cause);
	}
}