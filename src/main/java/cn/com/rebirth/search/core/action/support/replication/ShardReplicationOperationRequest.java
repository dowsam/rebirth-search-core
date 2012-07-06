/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardReplicationOperationRequest.java 2012-3-29 15:01:07 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support.replication;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;


/**
 * The Class ShardReplicationOperationRequest.
 *
 * @author l.xue.nong
 */
public abstract class ShardReplicationOperationRequest implements ActionRequest {

	
	/** The Constant DEFAULT_TIMEOUT. */
	public static final TimeValue DEFAULT_TIMEOUT = new TimeValue(1, TimeUnit.MINUTES);

	
	/** The timeout. */
	protected TimeValue timeout = DEFAULT_TIMEOUT;

	
	/** The index. */
	protected String index;

	
	/** The threaded listener. */
	private boolean threadedListener = false;

	
	/** The threaded operation. */
	private boolean threadedOperation = true;

	
	/** The replication type. */
	private ReplicationType replicationType = ReplicationType.DEFAULT;

	
	/** The consistency level. */
	private WriteConsistencyLevel consistencyLevel = WriteConsistencyLevel.DEFAULT;

	
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
	 * @return the shard replication operation request
	 */
	public ShardReplicationOperationRequest index(String index) {
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
	public ShardReplicationOperationRequest listenerThreaded(boolean threadedListener) {
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
	 * @return the shard replication operation request
	 */
	public ShardReplicationOperationRequest operationThreaded(boolean threadedOperation) {
		this.threadedOperation = threadedOperation;
		return this;
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
	 * Replication type.
	 *
	 * @param replicationType the replication type
	 * @return the shard replication operation request
	 */
	public ShardReplicationOperationRequest replicationType(ReplicationType replicationType) {
		this.replicationType = replicationType;
		return this;
	}

	
	/**
	 * Consistency level.
	 *
	 * @return the write consistency level
	 */
	public WriteConsistencyLevel consistencyLevel() {
		return this.consistencyLevel;
	}

	
	/**
	 * Consistency level.
	 *
	 * @param consistencyLevel the consistency level
	 * @return the shard replication operation request
	 */
	public ShardReplicationOperationRequest consistencyLevel(WriteConsistencyLevel consistencyLevel) {
		this.consistencyLevel = consistencyLevel;
		return this;
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

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		replicationType = ReplicationType.fromId(in.readByte());
		consistencyLevel = WriteConsistencyLevel.fromId(in.readByte());
		timeout = TimeValue.readTimeValue(in);
		index = in.readUTF();
		
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeByte(replicationType.id());
		out.writeByte(consistencyLevel.id());
		timeout.writeTo(out);
		out.writeUTF(index);
	}

	
	/**
	 * Before local fork.
	 */
	public void beforeLocalFork() {

	}
}
