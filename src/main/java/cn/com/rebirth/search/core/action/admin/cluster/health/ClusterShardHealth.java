/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterShardHealth.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.health;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;

/**
 * The Class ClusterShardHealth.
 *
 * @author l.xue.nong
 */
public class ClusterShardHealth implements Streamable {

	/** The shard id. */
	private int shardId;

	/** The status. */
	ClusterHealthStatus status = ClusterHealthStatus.RED;

	/** The active shards. */
	int activeShards = 0;

	/** The relocating shards. */
	int relocatingShards = 0;

	/** The initializing shards. */
	int initializingShards = 0;

	/** The unassigned shards. */
	int unassignedShards = 0;

	/** The primary active. */
	boolean primaryActive = false;

	/**
	 * Instantiates a new cluster shard health.
	 */
	private ClusterShardHealth() {

	}

	/**
	 * Instantiates a new cluster shard health.
	 *
	 * @param shardId the shard id
	 */
	ClusterShardHealth(int shardId) {
		this.shardId = shardId;
	}

	/**
	 * Id.
	 *
	 * @return the int
	 */
	public int id() {
		return shardId;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return id();
	}

	/**
	 * Status.
	 *
	 * @return the cluster health status
	 */
	public ClusterHealthStatus status() {
		return status;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public ClusterHealthStatus getStatus() {
		return status();
	}

	/**
	 * Relocating shards.
	 *
	 * @return the int
	 */
	public int relocatingShards() {
		return relocatingShards;
	}

	/**
	 * Gets the relocating shards.
	 *
	 * @return the relocating shards
	 */
	public int getRelocatingShards() {
		return relocatingShards();
	}

	/**
	 * Active shards.
	 *
	 * @return the int
	 */
	public int activeShards() {
		return activeShards;
	}

	/**
	 * Gets the active shards.
	 *
	 * @return the active shards
	 */
	public int getActiveShards() {
		return activeShards();
	}

	/**
	 * Primary active.
	 *
	 * @return true, if successful
	 */
	public boolean primaryActive() {
		return primaryActive;
	}

	/**
	 * Checks if is primary active.
	 *
	 * @return true, if is primary active
	 */
	public boolean isPrimaryActive() {
		return primaryActive();
	}

	/**
	 * Initializing shards.
	 *
	 * @return the int
	 */
	public int initializingShards() {
		return initializingShards;
	}

	/**
	 * Gets the initializing shards.
	 *
	 * @return the initializing shards
	 */
	public int getInitializingShards() {
		return initializingShards();
	}

	/**
	 * Unassigned shards.
	 *
	 * @return the int
	 */
	public int unassignedShards() {
		return unassignedShards;
	}

	/**
	 * Gets the unassigned shards.
	 *
	 * @return the unassigned shards
	 */
	public int getUnassignedShards() {
		return unassignedShards();
	}

	/**
	 * Read cluster shard health.
	 *
	 * @param in the in
	 * @return the cluster shard health
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	static ClusterShardHealth readClusterShardHealth(StreamInput in) throws IOException {
		ClusterShardHealth ret = new ClusterShardHealth();
		ret.readFrom(in);
		return ret;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		shardId = in.readVInt();
		status = ClusterHealthStatus.fromValue(in.readByte());
		activeShards = in.readVInt();
		relocatingShards = in.readVInt();
		initializingShards = in.readVInt();
		unassignedShards = in.readVInt();
		primaryActive = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(shardId);
		out.writeByte(status.value());
		out.writeVInt(activeShards);
		out.writeVInt(relocatingShards);
		out.writeVInt(initializingShards);
		out.writeVInt(unassignedShards);
		out.writeBoolean(primaryActive);
	}
}
