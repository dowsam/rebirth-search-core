/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesReplicationOperationRequest.java 2012-3-29 15:00:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support.replication;

import java.io.IOException;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;


/**
 * The Class IndicesReplicationOperationRequest.
 *
 * @author l.xue.nong
 */
public class IndicesReplicationOperationRequest implements ActionRequest {

	
	/** The timeout. */
	protected TimeValue timeout = ShardReplicationOperationRequest.DEFAULT_TIMEOUT;

	
	/** The indices. */
	protected String[] indices;

	
	/** The threaded listener. */
	private boolean threadedListener = false;

	
	/** The routing. */
	@Nullable
	private String routing;

	
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
	 * Indices.
	 *
	 * @return the string[]
	 */
	public String[] indices() {
		return this.indices;
	}

	
	/**
	 * Indices.
	 *
	 * @param indices the indices
	 * @return the indices replication operation request
	 */
	public IndicesReplicationOperationRequest indices(String[] indices) {
		this.indices = indices;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		return null;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded()
	 */
	@Override
	public boolean listenerThreaded() {
		return this.threadedListener;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded(boolean)
	 */
	@Override
	public IndicesReplicationOperationRequest listenerThreaded(boolean threadedListener) {
		this.threadedListener = threadedListener;
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
	 * Consistency level.
	 *
	 * @return the write consistency level
	 */
	public WriteConsistencyLevel consistencyLevel() {
		return this.consistencyLevel;
	}

	
	/**
	 * Routing.
	 *
	 * @return the string
	 */
	public String routing() {
		return null;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		replicationType = ReplicationType.fromId(in.readByte());
		consistencyLevel = WriteConsistencyLevel.fromId(in.readByte());
		timeout = TimeValue.readTimeValue(in);
		indices = new String[in.readVInt()];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = in.readUTF();
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeByte(replicationType.id());
		out.writeByte(consistencyLevel.id());
		timeout.writeTo(out);
		out.writeVInt(indices.length);
		for (String index : indices) {
			out.writeUTF(index);
		}
	}
}