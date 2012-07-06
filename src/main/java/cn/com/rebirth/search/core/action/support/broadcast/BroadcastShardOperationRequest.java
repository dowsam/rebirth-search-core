/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BroadcastShardOperationRequest.java 2012-7-6 14:29:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.broadcast;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;

/**
 * The Class BroadcastShardOperationRequest.
 *
 * @author l.xue.nong
 */
public abstract class BroadcastShardOperationRequest implements Streamable {

	/** The index. */
	private String index;

	/** The shard id. */
	private int shardId;

	/**
	 * Instantiates a new broadcast shard operation request.
	 */
	protected BroadcastShardOperationRequest() {
	}

	/**
	 * Instantiates a new broadcast shard operation request.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 */
	protected BroadcastShardOperationRequest(String index, int shardId) {
		this.index = index;
		this.shardId = shardId;
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
	 * Shard id.
	 *
	 * @return the int
	 */
	public int shardId() {
		return this.shardId;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		index = in.readUTF();
		shardId = in.readVInt();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(index);
		out.writeVInt(shardId);
	}
}
