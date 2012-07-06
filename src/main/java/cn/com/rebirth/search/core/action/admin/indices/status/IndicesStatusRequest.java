/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesStatusRequest.java 2012-3-29 15:01:08 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.status;

import java.io.IOException;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;


/**
 * The Class IndicesStatusRequest.
 *
 * @author l.xue.nong
 */
public class IndicesStatusRequest extends BroadcastOperationRequest {

	
	/** The recovery. */
	private boolean recovery = false;

	
	/** The snapshot. */
	private boolean snapshot = false;

	
	/**
	 * Instantiates a new indices status request.
	 */
	public IndicesStatusRequest() {
		this(Strings.EMPTY_ARRAY);
	}

	
	/**
	 * Instantiates a new indices status request.
	 *
	 * @param indices the indices
	 */
	public IndicesStatusRequest(String... indices) {
		super(indices);
	}

	
	/**
	 * Recovery.
	 *
	 * @param recovery the recovery
	 * @return the indices status request
	 */
	public IndicesStatusRequest recovery(boolean recovery) {
		this.recovery = recovery;
		return this;
	}

	
	/**
	 * Recovery.
	 *
	 * @return true, if successful
	 */
	public boolean recovery() {
		return this.recovery;
	}

	
	/**
	 * Snapshot.
	 *
	 * @param snapshot the snapshot
	 * @return the indices status request
	 */
	public IndicesStatusRequest snapshot(boolean snapshot) {
		this.snapshot = snapshot;
		return this;
	}

	
	/**
	 * Snapshot.
	 *
	 * @return true, if successful
	 */
	public boolean snapshot() {
		return this.snapshot;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#listenerThreaded(boolean)
	 */
	@Override
	public IndicesStatusRequest listenerThreaded(boolean listenerThreaded) {
		super.listenerThreaded(listenerThreaded);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#operationThreading(cn.com.summall.search.core.action.support.broadcast.BroadcastOperationThreading)
	 */
	@Override
	public BroadcastOperationRequest operationThreading(BroadcastOperationThreading operationThreading) {
		return super.operationThreading(operationThreading);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(recovery);
		out.writeBoolean(snapshot);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		recovery = in.readBoolean();
		snapshot = in.readBoolean();
	}
}
