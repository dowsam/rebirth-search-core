/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardCountResponse.java 2012-3-29 15:01:04 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.count;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationResponse;


/**
 * The Class ShardCountResponse.
 *
 * @author l.xue.nong
 */
class ShardCountResponse extends BroadcastShardOperationResponse {

	
	/** The count. */
	private long count;

	
	/**
	 * Instantiates a new shard count response.
	 */
	ShardCountResponse() {

	}

	
	/**
	 * Instantiates a new shard count response.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 * @param count the count
	 */
	public ShardCountResponse(String index, int shardId, long count) {
		super(index, shardId);
		this.count = count;
	}

	
	/**
	 * Count.
	 *
	 * @return the long
	 */
	long count() {
		return this.count;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationResponse#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		count = in.readVLong();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationResponse#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeVLong(count);
	}
}
