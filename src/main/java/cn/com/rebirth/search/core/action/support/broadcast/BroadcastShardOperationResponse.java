/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BroadcastShardOperationResponse.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.broadcast;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;

/**
 * The Class BroadcastShardOperationResponse.
 *
 * @author l.xue.nong
 */
public abstract class BroadcastShardOperationResponse implements Streamable {

	/** The index. */
	String index;

	/** The shard id. */
	int shardId;

	/**
	 * Instantiates a new broadcast shard operation response.
	 */
	protected BroadcastShardOperationResponse() {

	}

	/**
	 * Instantiates a new broadcast shard operation response.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 */
	protected BroadcastShardOperationResponse(String index, int shardId) {
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
	 * Gets the index.
	 *
	 * @return the index
	 */
	public String getIndex() {
		return index();
	}

	/**
	 * Shard id.
	 *
	 * @return the int
	 */
	public int shardId() {
		return this.shardId;
	}

	/**
	 * Gets the shard id.
	 *
	 * @return the shard id
	 */
	public int getShardId() {
		return shardId();
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