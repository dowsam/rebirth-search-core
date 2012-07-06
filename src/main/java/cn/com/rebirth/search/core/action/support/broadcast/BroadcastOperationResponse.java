/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BroadcastOperationResponse.java 2012-7-6 14:29:49 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.broadcast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.ShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.DefaultShardOperationFailedException;

import com.google.common.collect.ImmutableList;

/**
 * The Class BroadcastOperationResponse.
 *
 * @author l.xue.nong
 */
public abstract class BroadcastOperationResponse implements ActionResponse {

	/** The total shards. */
	private int totalShards;

	/** The successful shards. */
	private int successfulShards;

	/** The failed shards. */
	private int failedShards;

	/** The shard failures. */
	private List<ShardOperationFailedException> shardFailures = ImmutableList.of();

	/**
	 * Instantiates a new broadcast operation response.
	 */
	protected BroadcastOperationResponse() {
	}

	/**
	 * Instantiates a new broadcast operation response.
	 *
	 * @param totalShards the total shards
	 * @param successfulShards the successful shards
	 * @param failedShards the failed shards
	 * @param shardFailures the shard failures
	 */
	protected BroadcastOperationResponse(int totalShards, int successfulShards, int failedShards,
			List<ShardOperationFailedException> shardFailures) {
		this.totalShards = totalShards;
		this.successfulShards = successfulShards;
		this.failedShards = failedShards;
		this.shardFailures = shardFailures;
		if (shardFailures == null) {
			this.shardFailures = ImmutableList.of();
		}
	}

	/**
	 * Total shards.
	 *
	 * @return the int
	 */
	public int totalShards() {
		return totalShards;
	}

	/**
	 * Gets the total shards.
	 *
	 * @return the total shards
	 */
	public int getTotalShards() {
		return totalShards;
	}

	/**
	 * Successful shards.
	 *
	 * @return the int
	 */
	public int successfulShards() {
		return successfulShards;
	}

	/**
	 * Gets the successful shards.
	 *
	 * @return the successful shards
	 */
	public int getSuccessfulShards() {
		return successfulShards;
	}

	/**
	 * Failed shards.
	 *
	 * @return the int
	 */
	public int failedShards() {
		return failedShards;
	}

	/**
	 * Gets the failed shards.
	 *
	 * @return the failed shards
	 */
	public int getFailedShards() {
		return failedShards;
	}

	/**
	 * Shard failures.
	 *
	 * @return the list<? extends shard operation failed exception>
	 */
	public List<? extends ShardOperationFailedException> shardFailures() {
		if (shardFailures == null) {
			return ImmutableList.of();
		}
		return shardFailures;
	}

	/**
	 * Gets the shard failures.
	 *
	 * @return the shard failures
	 */
	public List<ShardOperationFailedException> getShardFailures() {
		return shardFailures;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		totalShards = in.readVInt();
		successfulShards = in.readVInt();
		failedShards = in.readVInt();
		int size = in.readVInt();
		if (size > 0) {
			shardFailures = new ArrayList<ShardOperationFailedException>(size);
			for (int i = 0; i < size; i++) {
				shardFailures.add(DefaultShardOperationFailedException.readShardOperationFailed(in));
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(totalShards);
		out.writeVInt(successfulShards);
		out.writeVInt(failedShards);
		out.writeVInt(shardFailures.size());
		for (ShardOperationFailedException exp : shardFailures) {
			exp.writeTo(out);
		}
	}
}
