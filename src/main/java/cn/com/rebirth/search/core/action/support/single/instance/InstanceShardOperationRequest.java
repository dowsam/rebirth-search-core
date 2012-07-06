/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InstanceShardOperationRequest.java 2012-7-6 14:29:15 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.single.instance;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;

/**
 * The Class InstanceShardOperationRequest.
 *
 * @author l.xue.nong
 */
public abstract class InstanceShardOperationRequest implements ActionRequest {

	/** The Constant DEFAULT_TIMEOUT. */
	public static final TimeValue DEFAULT_TIMEOUT = new TimeValue(1, TimeUnit.MINUTES);

	/** The timeout. */
	protected TimeValue timeout = DEFAULT_TIMEOUT;

	/** The index. */
	protected String index;

	/** The shard id. */
	protected int shardId = -1;

	/** The threaded listener. */
	private boolean threadedListener = false;

	/**
	 * Instantiates a new instance shard operation request.
	 */
	protected InstanceShardOperationRequest() {
	}

	/**
	 * Instantiates a new instance shard operation request.
	 *
	 * @param index the index
	 */
	public InstanceShardOperationRequest(String index) {
		this.index = index;
	}

	/**
	 * Timeout.
	 *
	 * @return the time value
	 */
	public TimeValue timeout() {
		return timeout;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
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
	 * @return the instance shard operation request
	 */
	InstanceShardOperationRequest index(String index) {
		this.index = index;
		return this;
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
	public InstanceShardOperationRequest listenerThreaded(boolean threadedListener) {
		this.threadedListener = threadedListener;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		index = in.readUTF();
		shardId = in.readInt();
		timeout = TimeValue.readTimeValue(in);

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(index);
		out.writeInt(shardId);
		timeout.writeTo(out);
	}

	/**
	 * Before local fork.
	 */
	public void beforeLocalFork() {
	}
}
