/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RecoveryFinalizeRecoveryRequest.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.recovery;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class RecoveryFinalizeRecoveryRequest.
 *
 * @author l.xue.nong
 */
class RecoveryFinalizeRecoveryRequest implements Streamable {

	/** The shard id. */
	private ShardId shardId;

	/**
	 * Instantiates a new recovery finalize recovery request.
	 */
	RecoveryFinalizeRecoveryRequest() {
	}

	/**
	 * Instantiates a new recovery finalize recovery request.
	 *
	 * @param shardId the shard id
	 */
	RecoveryFinalizeRecoveryRequest(ShardId shardId) {
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
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		shardId = ShardId.readShardId(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		shardId.writeTo(out);
	}
}
