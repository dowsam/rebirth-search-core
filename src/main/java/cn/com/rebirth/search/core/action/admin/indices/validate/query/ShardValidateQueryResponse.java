/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardValidateQueryResponse.java 2012-7-6 14:29:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.validate.query;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationResponse;

/**
 * The Class ShardValidateQueryResponse.
 *
 * @author l.xue.nong
 */
class ShardValidateQueryResponse extends BroadcastShardOperationResponse {

	/** The valid. */
	private boolean valid;

	/**
	 * Instantiates a new shard validate query response.
	 */
	ShardValidateQueryResponse() {

	}

	/**
	 * Instantiates a new shard validate query response.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 * @param valid the valid
	 */
	public ShardValidateQueryResponse(String index, int shardId, boolean valid) {
		super(index, shardId);
		this.valid = valid;
	}

	/**
	 * Valid.
	 *
	 * @return true, if successful
	 */
	boolean valid() {
		return this.valid;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		valid = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(valid);
	}
}
