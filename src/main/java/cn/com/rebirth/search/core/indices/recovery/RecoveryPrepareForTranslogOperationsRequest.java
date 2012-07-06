/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RecoveryPrepareForTranslogOperationsRequest.java 2012-3-29 15:02:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.recovery;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class RecoveryPrepareForTranslogOperationsRequest.
 *
 * @author l.xue.nong
 */
class RecoveryPrepareForTranslogOperationsRequest implements Streamable {

	/** The shard id. */
	private ShardId shardId;

	/**
	 * Instantiates a new recovery prepare for translog operations request.
	 */
	RecoveryPrepareForTranslogOperationsRequest() {
	}

	/**
	 * Instantiates a new recovery prepare for translog operations request.
	 *
	 * @param shardId the shard id
	 */
	RecoveryPrepareForTranslogOperationsRequest(ShardId shardId) {
		this.shardId = shardId;
	}

	/**
	 * Shard id.
	 *
	 * @return the shard id
	 */
	public ShardId shardId() {
		return shardId;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		shardId = ShardId.readShardId(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		shardId.writeTo(out);
	}
}
