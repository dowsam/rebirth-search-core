/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterIndexHealth.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.health;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * The Class ClusterIndexHealth.
 *
 * @author l.xue.nong
 */
public class ClusterIndexHealth implements Iterable<ClusterShardHealth>, Streamable {

	/** The index. */
	private String index;

	/** The number of shards. */
	private int numberOfShards;

	/** The number of replicas. */
	private int numberOfReplicas;

	/** The active shards. */
	int activeShards = 0;

	/** The relocating shards. */
	int relocatingShards = 0;

	/** The initializing shards. */
	int initializingShards = 0;

	/** The unassigned shards. */
	int unassignedShards = 0;

	/** The active primary shards. */
	int activePrimaryShards = 0;

	/** The status. */
	ClusterHealthStatus status = ClusterHealthStatus.RED;

	/** The shards. */
	final Map<Integer, ClusterShardHealth> shards = Maps.newHashMap();

	/** The validation failures. */
	List<String> validationFailures;

	/**
	 * Instantiates a new cluster index health.
	 */
	private ClusterIndexHealth() {
	}

	/**
	 * Instantiates a new cluster index health.
	 *
	 * @param index the index
	 * @param numberOfShards the number of shards
	 * @param numberOfReplicas the number of replicas
	 * @param validationFailures the validation failures
	 */
	public ClusterIndexHealth(String index, int numberOfShards, int numberOfReplicas, List<String> validationFailures) {
		this.index = index;
		this.numberOfShards = numberOfShards;
		this.numberOfReplicas = numberOfReplicas;
		this.validationFailures = validationFailures;
	}

	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return index;
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
	 * Validation failures.
	 *
	 * @return the list
	 */
	public List<String> validationFailures() {
		return this.validationFailures;
	}

	/**
	 * Gets the validation failures.
	 *
	 * @return the validation failures
	 */
	public List<String> getValidationFailures() {
		return validationFailures();
	}

	/**
	 * Number of shards.
	 *
	 * @return the int
	 */
	public int numberOfShards() {
		return numberOfShards;
	}

	/**
	 * Gets the number of shards.
	 *
	 * @return the number of shards
	 */
	public int getNumberOfShards() {
		return numberOfShards();
	}

	/**
	 * Number of replicas.
	 *
	 * @return the int
	 */
	public int numberOfReplicas() {
		return numberOfReplicas;
	}

	/**
	 * Gets the number of replicas.
	 *
	 * @return the number of replicas
	 */
	public int getNumberOfReplicas() {
		return numberOfReplicas();
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
	 * Active primary shards.
	 *
	 * @return the int
	 */
	public int activePrimaryShards() {
		return activePrimaryShards;
	}

	/**
	 * Gets the active primary shards.
	 *
	 * @return the active primary shards
	 */
	public int getActivePrimaryShards() {
		return activePrimaryShards();
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
	 * Shards.
	 *
	 * @return the map
	 */
	public Map<Integer, ClusterShardHealth> shards() {
		return this.shards;
	}

	/**
	 * Gets the shards.
	 *
	 * @return the shards
	 */
	public Map<Integer, ClusterShardHealth> getShards() {
		return shards();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ClusterShardHealth> iterator() {
		return shards.values().iterator();
	}

	/**
	 * Read cluster index health.
	 *
	 * @param in the in
	 * @return the cluster index health
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ClusterIndexHealth readClusterIndexHealth(StreamInput in) throws IOException {
		ClusterIndexHealth indexHealth = new ClusterIndexHealth();
		indexHealth.readFrom(in);
		return indexHealth;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		index = in.readUTF();
		numberOfShards = in.readVInt();
		numberOfReplicas = in.readVInt();
		activePrimaryShards = in.readVInt();
		activeShards = in.readVInt();
		relocatingShards = in.readVInt();
		initializingShards = in.readVInt();
		unassignedShards = in.readVInt();
		status = ClusterHealthStatus.fromValue(in.readByte());

		int size = in.readVInt();
		for (int i = 0; i < size; i++) {
			ClusterShardHealth shardHealth = ClusterShardHealth.readClusterShardHealth(in);
			shards.put(shardHealth.id(), shardHealth);
		}
		size = in.readVInt();
		if (size == 0) {
			validationFailures = ImmutableList.of();
		} else {
			for (int i = 0; i < size; i++) {
				validationFailures.add(in.readUTF());
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(index);
		out.writeVInt(numberOfShards);
		out.writeVInt(numberOfReplicas);
		out.writeVInt(activePrimaryShards);
		out.writeVInt(activeShards);
		out.writeVInt(relocatingShards);
		out.writeVInt(initializingShards);
		out.writeVInt(unassignedShards);
		out.writeByte(status.value());

		out.writeVInt(shards.size());
		for (ClusterShardHealth shardHealth : this) {
			shardHealth.writeTo(out);
		}

		out.writeVInt(validationFailures.size());
		for (String failure : validationFailures) {
			out.writeUTF(failure);
		}
	}
}
