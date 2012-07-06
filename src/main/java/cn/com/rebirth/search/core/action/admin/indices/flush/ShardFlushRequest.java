/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardFlushRequest.java 2012-7-6 14:30:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.flush;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest;

/**
 * The Class ShardFlushRequest.
 *
 * @author l.xue.nong
 */
class ShardFlushRequest extends BroadcastShardOperationRequest {

	/** The refresh. */
	private boolean refresh;

	/** The full. */
	private boolean full;

	/** The force. */
	private boolean force;

	/**
	 * Instantiates a new shard flush request.
	 */
	ShardFlushRequest() {
	}

	/**
	 * Instantiates a new shard flush request.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 * @param request the request
	 */
	public ShardFlushRequest(String index, int shardId, FlushRequest request) {
		super(index, shardId);
		this.refresh = request.refresh();
		this.full = request.full();
		this.force = request.force();
	}

	/**
	 * Refresh.
	 *
	 * @return true, if successful
	 */
	public boolean refresh() {
		return this.refresh;
	}

	/**
	 * Full.
	 *
	 * @return true, if successful
	 */
	public boolean full() {
		return this.full;
	}

	/**
	 * Force.
	 *
	 * @return true, if successful
	 */
	public boolean force() {
		return this.force;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		refresh = in.readBoolean();
		full = in.readBoolean();
		force = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(refresh);
		out.writeBoolean(full);
		out.writeBoolean(force);
	}
}