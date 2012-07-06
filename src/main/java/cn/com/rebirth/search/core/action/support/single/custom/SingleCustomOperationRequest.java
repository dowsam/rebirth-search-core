/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SingleCustomOperationRequest.java 2012-7-6 14:30:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.single.custom;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;

/**
 * The Class SingleCustomOperationRequest.
 *
 * @author l.xue.nong
 */
public abstract class SingleCustomOperationRequest implements ActionRequest {

	/** The threaded listener. */
	private boolean threadedListener = false;

	/** The threaded operation. */
	private boolean threadedOperation = true;

	/** The prefer local. */
	private boolean preferLocal = true;

	/**
	 * Instantiates a new single custom operation request.
	 */
	protected SingleCustomOperationRequest() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#listenerThreaded()
	 */
	@Override
	public boolean listenerThreaded() {
		return threadedListener;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#listenerThreaded(boolean)
	 */
	@Override
	public SingleCustomOperationRequest listenerThreaded(boolean threadedListener) {
		this.threadedListener = threadedListener;
		return this;
	}

	/**
	 * Operation threaded.
	 *
	 * @return true, if successful
	 */
	public boolean operationThreaded() {
		return threadedOperation;
	}

	/**
	 * Operation threaded.
	 *
	 * @param threadedOperation the threaded operation
	 * @return the single custom operation request
	 */
	public SingleCustomOperationRequest operationThreaded(boolean threadedOperation) {
		this.threadedOperation = threadedOperation;
		return this;
	}

	/**
	 * Prefer local.
	 *
	 * @param preferLocal the prefer local
	 * @return the single custom operation request
	 */
	public SingleCustomOperationRequest preferLocal(boolean preferLocal) {
		this.preferLocal = preferLocal;
		return this;
	}

	/**
	 * Prefer local shard.
	 *
	 * @return true, if successful
	 */
	public boolean preferLocalShard() {
		return this.preferLocal;
	}

	/**
	 * Before local fork.
	 */
	public void beforeLocalFork() {

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {

		preferLocal = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeBoolean(preferLocal);
	}
}
