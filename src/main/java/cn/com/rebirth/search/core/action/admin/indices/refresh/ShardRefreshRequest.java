/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardRefreshRequest.java 2012-3-29 15:01:44 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.refresh;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest;


/**
 * The Class ShardRefreshRequest.
 *
 * @author l.xue.nong
 */
class ShardRefreshRequest extends BroadcastShardOperationRequest {

	
	/** The wait for operations. */
	private boolean waitForOperations = true;

	
	/**
	 * Instantiates a new shard refresh request.
	 */
	ShardRefreshRequest() {
	}

	
	/**
	 * Instantiates a new shard refresh request.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 * @param request the request
	 */
	public ShardRefreshRequest(String index, int shardId, RefreshRequest request) {
		super(index, shardId);
		waitForOperations = request.waitForOperations();
	}

	
	/**
	 * Wait for operations.
	 *
	 * @return true, if successful
	 */
	public boolean waitForOperations() {
		return waitForOperations;
	}

	
	/**
	 * Wait for operations.
	 *
	 * @param waitForOperations the wait for operations
	 * @return the shard refresh request
	 */
	public ShardRefreshRequest waitForOperations(boolean waitForOperations) {
		this.waitForOperations = waitForOperations;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		waitForOperations = in.readBoolean();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(waitForOperations);
	}
}