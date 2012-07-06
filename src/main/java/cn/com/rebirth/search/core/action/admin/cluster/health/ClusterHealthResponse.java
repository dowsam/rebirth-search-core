/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterHealthResponse.java 2012-3-29 15:02:50 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.health;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionResponse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;


/**
 * The Class ClusterHealthResponse.
 *
 * @author l.xue.nong
 */
public class ClusterHealthResponse implements ActionResponse, Iterable<ClusterIndexHealth> {

	
	/** The cluster name. */
	private String clusterName;

	
	/** The number of nodes. */
	int numberOfNodes = 0;

	
	/** The number of data nodes. */
	int numberOfDataNodes = 0;

	
	/** The active shards. */
	int activeShards = 0;

	
	/** The relocating shards. */
	int relocatingShards = 0;

	
	/** The active primary shards. */
	int activePrimaryShards = 0;

	
	/** The initializing shards. */
	int initializingShards = 0;

	
	/** The unassigned shards. */
	int unassignedShards = 0;

	
	/** The timed out. */
	boolean timedOut = false;

	
	/** The status. */
	ClusterHealthStatus status = ClusterHealthStatus.RED;

	
	/** The validation failures. */
	private List<String> validationFailures;

	
	/** The indices. */
	Map<String, ClusterIndexHealth> indices = Maps.newHashMap();

	
	/**
	 * Instantiates a new cluster health response.
	 */
	ClusterHealthResponse() {
	}

	
	/**
	 * Instantiates a new cluster health response.
	 *
	 * @param clusterName the cluster name
	 * @param validationFailures the validation failures
	 */
	public ClusterHealthResponse(String clusterName, List<String> validationFailures) {
		this.clusterName = clusterName;
		this.validationFailures = validationFailures;
	}

	
	/**
	 * Cluster name.
	 *
	 * @return the string
	 */
	public String clusterName() {
		return clusterName;
	}

	
	/**
	 * Gets the cluster name.
	 *
	 * @return the cluster name
	 */
	public String getClusterName() {
		return clusterName();
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
	 * All validation failures.
	 *
	 * @return the list
	 */
	public List<String> allValidationFailures() {
		List<String> allFailures = newArrayList(validationFailures());
		for (ClusterIndexHealth indexHealth : indices.values()) {
			allFailures.addAll(indexHealth.validationFailures());
		}
		return allFailures;
	}

	
	/**
	 * Gets the all validation failures.
	 *
	 * @return the all validation failures
	 */
	public List<String> getAllValidationFailures() {
		return allValidationFailures();
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
	 * Number of nodes.
	 *
	 * @return the int
	 */
	public int numberOfNodes() {
		return this.numberOfNodes;
	}

	
	/**
	 * Gets the number of nodes.
	 *
	 * @return the number of nodes
	 */
	public int getNumberOfNodes() {
		return numberOfNodes();
	}

	
	/**
	 * Number of data nodes.
	 *
	 * @return the int
	 */
	public int numberOfDataNodes() {
		return this.numberOfDataNodes;
	}

	
	/**
	 * Gets the number of data nodes.
	 *
	 * @return the number of data nodes
	 */
	public int getNumberOfDataNodes() {
		return numberOfDataNodes();
	}

	
	/**
	 * Timed out.
	 *
	 * @return true, if successful
	 */
	public boolean timedOut() {
		return this.timedOut;
	}

	
	/**
	 * Checks if is timed out.
	 *
	 * @return true, if is timed out
	 */
	public boolean isTimedOut() {
		return this.timedOut();
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
	 * Indices.
	 *
	 * @return the map
	 */
	public Map<String, ClusterIndexHealth> indices() {
		return indices;
	}

	
	/**
	 * Gets the indices.
	 *
	 * @return the indices
	 */
	public Map<String, ClusterIndexHealth> getIndices() {
		return indices();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ClusterIndexHealth> iterator() {
		return indices.values().iterator();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		clusterName = in.readUTF();
		activePrimaryShards = in.readVInt();
		activeShards = in.readVInt();
		relocatingShards = in.readVInt();
		initializingShards = in.readVInt();
		unassignedShards = in.readVInt();
		numberOfNodes = in.readVInt();
		numberOfDataNodes = in.readVInt();
		status = ClusterHealthStatus.fromValue(in.readByte());
		int size = in.readVInt();
		for (int i = 0; i < size; i++) {
			ClusterIndexHealth indexHealth = ClusterIndexHealth.readClusterIndexHealth(in);
			indices.put(indexHealth.index(), indexHealth);
		}
		timedOut = in.readBoolean();
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
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(clusterName);
		out.writeVInt(activePrimaryShards);
		out.writeVInt(activeShards);
		out.writeVInt(relocatingShards);
		out.writeVInt(initializingShards);
		out.writeVInt(unassignedShards);
		out.writeVInt(numberOfNodes);
		out.writeVInt(numberOfDataNodes);
		out.writeByte(status.value());
		out.writeVInt(indices.size());
		for (ClusterIndexHealth indexHealth : this) {
			indexHealth.writeTo(out);
		}
		out.writeBoolean(timedOut);

		out.writeVInt(validationFailures.size());
		for (String failure : validationFailures) {
			out.writeUTF(failure);
		}
	}

}
