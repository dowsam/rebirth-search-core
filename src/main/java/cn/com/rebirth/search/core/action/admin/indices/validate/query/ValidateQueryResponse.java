/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ValidateQueryResponse.java 2012-7-6 14:29:53 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.validate.query;

import java.io.IOException;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse;

/**
 * The Class ValidateQueryResponse.
 *
 * @author l.xue.nong
 */
public class ValidateQueryResponse extends BroadcastOperationResponse {

	/** The valid. */
	private boolean valid;

	/**
	 * Instantiates a new validate query response.
	 */
	ValidateQueryResponse() {

	}

	/**
	 * Instantiates a new validate query response.
	 *
	 * @param valid the valid
	 * @param totalShards the total shards
	 * @param successfulShards the successful shards
	 * @param failedShards the failed shards
	 * @param shardFailures the shard failures
	 */
	ValidateQueryResponse(boolean valid, int totalShards, int successfulShards, int failedShards,
			List<ShardOperationFailedException> shardFailures) {
		super(totalShards, successfulShards, failedShards, shardFailures);
		this.valid = valid;
	}

	/**
	 * Valid.
	 *
	 * @return true, if successful
	 */
	public boolean valid() {
		return valid;
	}

	/**
	 * Gets the valid.
	 *
	 * @return the valid
	 */
	public boolean getValid() {
		return valid;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		valid = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(valid);
	}
}
