/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardOptimizeRequest.java 2012-7-6 14:28:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.optimize;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest;

/**
 * The Class ShardOptimizeRequest.
 *
 * @author l.xue.nong
 */
class ShardOptimizeRequest extends BroadcastShardOperationRequest {

	/** The wait for merge. */
	private boolean waitForMerge = OptimizeRequest.Defaults.WAIT_FOR_MERGE;

	/** The max num segments. */
	private int maxNumSegments = OptimizeRequest.Defaults.MAX_NUM_SEGMENTS;

	/** The only expunge deletes. */
	private boolean onlyExpungeDeletes = OptimizeRequest.Defaults.ONLY_EXPUNGE_DELETES;

	/** The flush. */
	private boolean flush = OptimizeRequest.Defaults.FLUSH;

	/** The refresh. */
	private boolean refresh = OptimizeRequest.Defaults.REFRESH;

	/**
	 * Instantiates a new shard optimize request.
	 */
	ShardOptimizeRequest() {
	}

	/**
	 * Instantiates a new shard optimize request.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 * @param request the request
	 */
	public ShardOptimizeRequest(String index, int shardId, OptimizeRequest request) {
		super(index, shardId);
		waitForMerge = request.waitForMerge();
		maxNumSegments = request.maxNumSegments();
		onlyExpungeDeletes = request.onlyExpungeDeletes();
		flush = request.flush();
		refresh = request.refresh();
	}

	/**
	 * Wait for merge.
	 *
	 * @return true, if successful
	 */
	boolean waitForMerge() {
		return waitForMerge;
	}

	/**
	 * Max num segments.
	 *
	 * @return the int
	 */
	int maxNumSegments() {
		return maxNumSegments;
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
	 * Flush.
	 *
	 * @return true, if successful
	 */
	public boolean flush() {
		return flush;
	}

	/**
	 * Refresh.
	 *
	 * @return true, if successful
	 */
	public boolean refresh() {
		return refresh;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		waitForMerge = in.readBoolean();
		maxNumSegments = in.readInt();
		onlyExpungeDeletes = in.readBoolean();
		flush = in.readBoolean();
		refresh = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(waitForMerge);
		out.writeInt(maxNumSegments);
		out.writeBoolean(onlyExpungeDeletes);
		out.writeBoolean(flush);
		out.writeBoolean(refresh);
	}
}