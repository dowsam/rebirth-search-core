/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SingleShardOperationRequest.java 2012-3-29 15:02:23 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support.single.shard;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;


/**
 * The Class SingleShardOperationRequest.
 *
 * @author l.xue.nong
 */
public abstract class SingleShardOperationRequest implements ActionRequest {

	
	/** The index. */
	protected String index;

	
	/** The threaded listener. */
	private boolean threadedListener = false;

	
	/** The threaded operation. */
	private boolean threadedOperation = true;

	
	/**
	 * Instantiates a new single shard operation request.
	 */
	protected SingleShardOperationRequest() {
	}

	
	/**
	 * Instantiates a new single shard operation request.
	 *
	 * @param index the index
	 */
	public SingleShardOperationRequest(String index) {
		this.index = index;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (index == null) {
			validationException = ValidateActions.addValidationError("index is missing", validationException);
		}
		return validationException;
	}

	
	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return index;
	}

	
	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the single shard operation request
	 */
	SingleShardOperationRequest index(String index) {
		this.index = index;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded()
	 */
	@Override
	public boolean listenerThreaded() {
		return threadedListener;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded(boolean)
	 */
	@Override
	public SingleShardOperationRequest listenerThreaded(boolean threadedListener) {
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
	 * @return the single shard operation request
	 */
	public SingleShardOperationRequest operationThreaded(boolean threadedOperation) {
		this.threadedOperation = threadedOperation;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		index = in.readUTF();
		
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(index);
	}

}
