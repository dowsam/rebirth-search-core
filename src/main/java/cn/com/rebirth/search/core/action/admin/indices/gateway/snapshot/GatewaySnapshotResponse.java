/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GatewaySnapshotResponse.java 2012-7-6 14:30:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot;

import java.io.IOException;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse;

/**
 * The Class GatewaySnapshotResponse.
 *
 * @author l.xue.nong
 */
public class GatewaySnapshotResponse extends BroadcastOperationResponse {

	/**
	 * Instantiates a new gateway snapshot response.
	 */
	GatewaySnapshotResponse() {

	}

	/**
	 * Instantiates a new gateway snapshot response.
	 *
	 * @param totalShards the total shards
	 * @param successfulShards the successful shards
	 * @param failedShards the failed shards
	 * @param shardFailures the shard failures
	 */
	GatewaySnapshotResponse(int totalShards, int successfulShards, int failedShards,
			List<ShardOperationFailedException> shardFailures) {
		super(totalShards, successfulShards, failedShards, shardFailures);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
	}
}
