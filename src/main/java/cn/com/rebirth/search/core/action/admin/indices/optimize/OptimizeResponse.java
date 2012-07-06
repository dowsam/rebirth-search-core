/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core OptimizeResponse.java 2012-3-29 15:01:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.optimize;

import java.io.IOException;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse;


/**
 * The Class OptimizeResponse.
 *
 * @author l.xue.nong
 */
public class OptimizeResponse extends BroadcastOperationResponse {

	
	/**
	 * Instantiates a new optimize response.
	 */
	OptimizeResponse() {

	}

	
	/**
	 * Instantiates a new optimize response.
	 *
	 * @param totalShards the total shards
	 * @param successfulShards the successful shards
	 * @param failedShards the failed shards
	 * @param shardFailures the shard failures
	 */
	OptimizeResponse(int totalShards, int successfulShards, int failedShards,
			List<ShardOperationFailedException> shardFailures) {
		super(totalShards, successfulShards, failedShards, shardFailures);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationResponse#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationResponse#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
	}
}