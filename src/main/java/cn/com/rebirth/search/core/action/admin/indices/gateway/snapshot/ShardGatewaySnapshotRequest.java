/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardGatewaySnapshotRequest.java 2012-7-6 14:29:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest;

/**
 * The Class ShardGatewaySnapshotRequest.
 *
 * @author l.xue.nong
 */
class ShardGatewaySnapshotRequest extends BroadcastShardOperationRequest {

	/**
	 * Instantiates a new shard gateway snapshot request.
	 */
	ShardGatewaySnapshotRequest() {
	}

	/**
	 * Instantiates a new shard gateway snapshot request.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 */
	public ShardGatewaySnapshotRequest(String index, int shardId) {
		super(index, shardId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
	}
}