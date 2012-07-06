/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BulkShardResponse.java 2012-3-29 15:02:42 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.bulk;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class BulkShardResponse.
 *
 * @author l.xue.nong
 */
public class BulkShardResponse implements ActionResponse {

	
	/** The shard id. */
	private ShardId shardId;

	
	/** The responses. */
	private BulkItemResponse[] responses;

	
	/**
	 * Instantiates a new bulk shard response.
	 */
	BulkShardResponse() {
	}

	
	/**
	 * Instantiates a new bulk shard response.
	 *
	 * @param shardId the shard id
	 * @param responses the responses
	 */
	BulkShardResponse(ShardId shardId, BulkItemResponse[] responses) {
		this.shardId = shardId;
		this.responses = responses;
	}

	
	/**
	 * Shard id.
	 *
	 * @return the shard id
	 */
	public ShardId shardId() {
		return shardId;
	}

	
	/**
	 * Responses.
	 *
	 * @return the bulk item response[]
	 */
	public BulkItemResponse[] responses() {
		return responses;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		shardId = ShardId.readShardId(in);
		responses = new BulkItemResponse[in.readVInt()];
		for (int i = 0; i < responses.length; i++) {
			responses[i] = BulkItemResponse.readBulkItem(in);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		shardId.writeTo(out);
		out.writeVInt(responses.length);
		for (BulkItemResponse response : responses) {
			response.writeTo(out);
		}
	}
}
