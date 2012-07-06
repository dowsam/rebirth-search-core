/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FlushRequest.java 2012-3-29 15:02:06 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.flush;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;


/**
 * The Class FlushRequest.
 *
 * @author l.xue.nong
 */
public class FlushRequest extends BroadcastOperationRequest {

	
	/** The refresh. */
	private boolean refresh = false;

	
	/** The force. */
	private boolean force = false;

	
	/** The full. */
	private boolean full = false;

	
	/**
	 * Instantiates a new flush request.
	 */
	FlushRequest() {

	}

	
	/**
	 * Instantiates a new flush request.
	 *
	 * @param indices the indices
	 */
	public FlushRequest(String... indices) {
		super(indices);
		
		operationThreading(BroadcastOperationThreading.THREAD_PER_SHARD);
	}

	
	/**
	 * Refresh.
	 *
	 * @return true, if successful
	 */
	public boolean refresh() {
		return this.refresh;
	}

	
	/**
	 * Refresh.
	 *
	 * @param refresh the refresh
	 * @return the flush request
	 */
	public FlushRequest refresh(boolean refresh) {
		this.refresh = refresh;
		return this;
	}

	
	/**
	 * Full.
	 *
	 * @return true, if successful
	 */
	public boolean full() {
		return this.full;
	}

	
	/**
	 * Full.
	 *
	 * @param full the full
	 * @return the flush request
	 */
	public FlushRequest full(boolean full) {
		this.full = full;
		return this;
	}

	
	/**
	 * Force.
	 *
	 * @return true, if successful
	 */
	public boolean force() {
		return force;
	}

	
	/**
	 * Force.
	 *
	 * @param force the force
	 * @return the flush request
	 */
	public FlushRequest force(boolean force) {
		this.force = force;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#listenerThreaded(boolean)
	 */
	@Override
	public FlushRequest listenerThreaded(boolean threadedListener) {
		super.listenerThreaded(threadedListener);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#operationThreading(cn.com.summall.search.core.action.support.broadcast.BroadcastOperationThreading)
	 */
	@Override
	public FlushRequest operationThreading(BroadcastOperationThreading operationThreading) {
		super.operationThreading(operationThreading);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(refresh);
		out.writeBoolean(full);
		out.writeBoolean(force);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		refresh = in.readBoolean();
		full = in.readBoolean();
		force = in.readBoolean();
	}
}
