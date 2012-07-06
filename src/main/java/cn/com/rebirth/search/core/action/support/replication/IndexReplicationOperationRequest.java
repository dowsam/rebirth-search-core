/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexReplicationOperationRequest.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.replication;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;

/**
 * The Class IndexReplicationOperationRequest.
 *
 * @author l.xue.nong
 */
public class IndexReplicationOperationRequest implements ActionRequest {

	/** The timeout. */
	protected TimeValue timeout = ShardReplicationOperationRequest.DEFAULT_TIMEOUT;

	/** The index. */
	protected String index;

	/** The threaded listener. */
	private boolean threadedListener = false;

	/** The replication type. */
	protected ReplicationType replicationType = ReplicationType.DEFAULT;

	/** The consistency level. */
	protected WriteConsistencyLevel consistencyLevel = WriteConsistencyLevel.DEFAULT;

	/**
	 * Timeout.
	 *
	 * @return the time value
	 */
	public TimeValue timeout() {
		return timeout;
	}

	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return this.index;
	}

	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the index replication operation request
	 */
	public IndexReplicationOperationRequest index(String index) {
		this.index = index;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#listenerThreaded()
	 */
	@Override
	public boolean listenerThreaded() {
		return this.threadedListener;
	}

	/**
	 * Replication type.
	 *
	 * @return the replication type
	 */
	public ReplicationType replicationType() {
		return this.replicationType;
	}

	/**
	 * Consistency level.
	 *
	 * @return the write consistency level
	 */
	public WriteConsistencyLevel consistencyLevel() {
		return this.consistencyLevel;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#listenerThreaded(boolean)
	 */
	@Override
	public IndexReplicationOperationRequest listenerThreaded(boolean threadedListener) {
		this.threadedListener = threadedListener;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (index == null) {
			validationException = ValidateActions.addValidationError("index name missing", validationException);
		}
		return validationException;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		replicationType = ReplicationType.fromId(in.readByte());
		consistencyLevel = WriteConsistencyLevel.fromId(in.readByte());
		timeout = TimeValue.readTimeValue(in);
		index = in.readUTF();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeByte(replicationType.id());
		out.writeByte(consistencyLevel.id());
		timeout.writeTo(out);
		out.writeUTF(index);
	}
}
