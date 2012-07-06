/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RefreshRequest.java 2012-7-6 14:28:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.refresh;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;

/**
 * The Class RefreshRequest.
 *
 * @author l.xue.nong
 */
public class RefreshRequest extends BroadcastOperationRequest {

	/** The wait for operations. */
	private boolean waitForOperations = true;

	/**
	 * Instantiates a new refresh request.
	 */
	RefreshRequest() {
	}

	/**
	 * Instantiates a new refresh request.
	 *
	 * @param indices the indices
	 */
	public RefreshRequest(String... indices) {
		super(indices);

		operationThreading(BroadcastOperationThreading.THREAD_PER_SHARD);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest#listenerThreaded(boolean)
	 */
	@Override
	public RefreshRequest listenerThreaded(boolean threadedListener) {
		super.listenerThreaded(threadedListener);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest#operationThreading(cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading)
	 */
	@Override
	public RefreshRequest operationThreading(BroadcastOperationThreading operationThreading) {
		super.operationThreading(operationThreading);
		return this;
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
	 * @return the refresh request
	 */
	public RefreshRequest waitForOperations(boolean waitForOperations) {
		this.waitForOperations = waitForOperations;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		waitForOperations = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(waitForOperations);
	}
}
