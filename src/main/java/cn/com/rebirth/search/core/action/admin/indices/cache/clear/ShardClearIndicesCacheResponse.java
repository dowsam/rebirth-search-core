/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardClearIndicesCacheResponse.java 2012-7-6 14:29:09 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.cache.clear;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationResponse;

/**
 * The Class ShardClearIndicesCacheResponse.
 *
 * @author l.xue.nong
 */
class ShardClearIndicesCacheResponse extends BroadcastShardOperationResponse {

	/**
	 * Instantiates a new shard clear indices cache response.
	 */
	ShardClearIndicesCacheResponse() {
	}

	/**
	 * Instantiates a new shard clear indices cache response.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 */
	public ShardClearIndicesCacheResponse(String index, int shardId) {
		super(index, shardId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
	}
}