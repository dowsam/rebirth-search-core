/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RecoveryTranslogOperationsRequest.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.recovery;

import java.io.IOException;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.index.translog.TranslogStreams;

import com.google.common.collect.Lists;

/**
 * The Class RecoveryTranslogOperationsRequest.
 *
 * @author l.xue.nong
 */
class RecoveryTranslogOperationsRequest implements Streamable {

	/** The shard id. */
	private ShardId shardId;

	/** The operations. */
	private List<Translog.Operation> operations;

	/**
	 * Instantiates a new recovery translog operations request.
	 */
	RecoveryTranslogOperationsRequest() {
	}

	/**
	 * Instantiates a new recovery translog operations request.
	 *
	 * @param shardId the shard id
	 * @param operations the operations
	 */
	RecoveryTranslogOperationsRequest(ShardId shardId, List<Translog.Operation> operations) {
		this.shardId = shardId;
		this.operations = operations;
	}

	/**
	 * Shard id.
	 *
	 * @return the shard id
	 */
	public ShardId shardId() {
		return shardId;
	}

	/**
	 * Operations.
	 *
	 * @return the list
	 */
	public List<Translog.Operation> operations() {
		return operations;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		shardId = ShardId.readShardId(in);
		int size = in.readVInt();
		operations = Lists.newArrayListWithExpectedSize(size);
		for (int i = 0; i < size; i++) {
			operations.add(TranslogStreams.readTranslogOperation(in));
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		shardId.writeTo(out);
		out.writeVInt(operations.size());
		for (Translog.Operation operation : operations) {
			TranslogStreams.writeTranslogOperation(out, operation);
		}
	}
}
