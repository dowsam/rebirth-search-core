/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core OptimizeRequest.java 2012-3-29 15:01:09 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.optimize;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;


/**
 * The Class OptimizeRequest.
 *
 * @author l.xue.nong
 */
public class OptimizeRequest extends BroadcastOperationRequest {

	
	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static final class Defaults {

		
		/** The Constant WAIT_FOR_MERGE. */
		public static final boolean WAIT_FOR_MERGE = true;

		
		/** The Constant MAX_NUM_SEGMENTS. */
		public static final int MAX_NUM_SEGMENTS = -1;

		
		/** The Constant ONLY_EXPUNGE_DELETES. */
		public static final boolean ONLY_EXPUNGE_DELETES = false;

		
		/** The Constant FLUSH. */
		public static final boolean FLUSH = true;

		
		/** The Constant REFRESH. */
		public static final boolean REFRESH = true;
	}

	
	/** The wait for merge. */
	private boolean waitForMerge = Defaults.WAIT_FOR_MERGE;

	
	/** The max num segments. */
	private int maxNumSegments = Defaults.MAX_NUM_SEGMENTS;

	
	/** The only expunge deletes. */
	private boolean onlyExpungeDeletes = Defaults.ONLY_EXPUNGE_DELETES;

	
	/** The flush. */
	private boolean flush = Defaults.FLUSH;

	
	/** The refresh. */
	private boolean refresh = Defaults.FLUSH;

	
	/**
	 * Instantiates a new optimize request.
	 *
	 * @param indices the indices
	 */
	public OptimizeRequest(String... indices) {
		super(indices);
		
		operationThreading(BroadcastOperationThreading.THREAD_PER_SHARD);
	}

	
	/**
	 * Instantiates a new optimize request.
	 */
	public OptimizeRequest() {

	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#listenerThreaded(boolean)
	 */
	@Override
	public OptimizeRequest listenerThreaded(boolean threadedListener) {
		super.listenerThreaded(threadedListener);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#operationThreading(cn.com.summall.search.core.action.support.broadcast.BroadcastOperationThreading)
	 */
	@Override
	public OptimizeRequest operationThreading(BroadcastOperationThreading operationThreading) {
		super.operationThreading(operationThreading);
		return this;
	}

	
	/**
	 * Wait for merge.
	 *
	 * @return true, if successful
	 */
	public boolean waitForMerge() {
		return waitForMerge;
	}

	
	/**
	 * Wait for merge.
	 *
	 * @param waitForMerge the wait for merge
	 * @return the optimize request
	 */
	public OptimizeRequest waitForMerge(boolean waitForMerge) {
		this.waitForMerge = waitForMerge;
		return this;
	}

	
	/**
	 * Max num segments.
	 *
	 * @return the int
	 */
	public int maxNumSegments() {
		return maxNumSegments;
	}

	
	/**
	 * Max num segments.
	 *
	 * @param maxNumSegments the max num segments
	 * @return the optimize request
	 */
	public OptimizeRequest maxNumSegments(int maxNumSegments) {
		this.maxNumSegments = maxNumSegments;
		return this;
	}

	
	/**
	 * Only expunge deletes.
	 *
	 * @return true, if successful
	 */
	public boolean onlyExpungeDeletes() {
		return onlyExpungeDeletes;
	}

	
	/**
	 * Only expunge deletes.
	 *
	 * @param onlyExpungeDeletes the only expunge deletes
	 * @return the optimize request
	 */
	public OptimizeRequest onlyExpungeDeletes(boolean onlyExpungeDeletes) {
		this.onlyExpungeDeletes = onlyExpungeDeletes;
		return this;
	}

	
	/**
	 * Flush.
	 *
	 * @return true, if successful
	 */
	public boolean flush() {
		return flush;
	}

	
	/**
	 * Flush.
	 *
	 * @param flush the flush
	 * @return the optimize request
	 */
	public OptimizeRequest flush(boolean flush) {
		this.flush = flush;
		return this;
	}

	
	/**
	 * Refresh.
	 *
	 * @return true, if successful
	 */
	public boolean refresh() {
		return refresh;
	}

	
	/**
	 * Refresh.
	 *
	 * @param refresh the refresh
	 * @return the optimize request
	 */
	public OptimizeRequest refresh(boolean refresh) {
		this.refresh = refresh;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		waitForMerge = in.readBoolean();
		maxNumSegments = in.readInt();
		onlyExpungeDeletes = in.readBoolean();
		flush = in.readBoolean();
		refresh = in.readBoolean();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(waitForMerge);
		out.writeInt(maxNumSegments);
		out.writeBoolean(onlyExpungeDeletes);
		out.writeBoolean(flush);
		out.writeBoolean(refresh);
	}
}