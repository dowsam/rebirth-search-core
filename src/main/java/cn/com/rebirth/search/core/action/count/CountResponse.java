/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CountResponse.java 2012-7-6 14:29:13 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.count;

import java.io.IOException;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse;

/**
 * The Class CountResponse.
 *
 * @author l.xue.nong
 */
public class CountResponse extends BroadcastOperationResponse {

	/** The count. */
	private long count;

	/**
	 * Instantiates a new count response.
	 */
	CountResponse() {

	}

	/**
	 * Instantiates a new count response.
	 *
	 * @param count the count
	 * @param totalShards the total shards
	 * @param successfulShards the successful shards
	 * @param failedShards the failed shards
	 * @param shardFailures the shard failures
	 */
	CountResponse(long count, int totalShards, int successfulShards, int failedShards,
			List<ShardOperationFailedException> shardFailures) {
		super(totalShards, successfulShards, failedShards, shardFailures);
		this.count = count;
	}

	/**
	 * Count.
	 *
	 * @return the long
	 */
	public long count() {
		return count;
	}

	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	public long getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		count = in.readVLong();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeVLong(count);
	}
}
