/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterHealthRequest.java 2012-3-29 15:00:51 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.health;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;


/**
 * The Class ClusterHealthRequest.
 *
 * @author l.xue.nong
 */
public class ClusterHealthRequest extends MasterNodeOperationRequest {

	
	/** The indices. */
	private String[] indices;

	
	/** The timeout. */
	private TimeValue timeout = new TimeValue(30, TimeUnit.SECONDS);

	
	/** The wait for status. */
	private ClusterHealthStatus waitForStatus;

	
	/** The wait for relocating shards. */
	private int waitForRelocatingShards = -1;

	
	/** The wait for active shards. */
	private int waitForActiveShards = -1;

	
	/** The wait for nodes. */
	private String waitForNodes = "";

	
	/**
	 * Instantiates a new cluster health request.
	 */
	ClusterHealthRequest() {
	}

	
	/**
	 * Instantiates a new cluster health request.
	 *
	 * @param indices the indices
	 */
	public ClusterHealthRequest(String... indices) {
		this.indices = indices;
	}

	
	/**
	 * Indices.
	 *
	 * @return the string[]
	 */
	public String[] indices() {
		return indices;
	}

	
	/**
	 * Indices.
	 *
	 * @param indices the indices
	 * @return the cluster health request
	 */
	public ClusterHealthRequest indices(String[] indices) {
		this.indices = indices;
		return this;
	}

	
	/**
	 * Timeout.
	 *
	 * @return the time value
	 */
	public TimeValue timeout() {
		return timeout;
	}

	
	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the cluster health request
	 */
	public ClusterHealthRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		if (masterNodeTimeout == DEFAULT_MASTER_NODE_TIMEOUT) {
			masterNodeTimeout = timeout;
		}
		return this;
	}

	
	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the cluster health request
	 */
	public ClusterHealthRequest timeout(String timeout) {
		return timeout(TimeValue.parseTimeValue(timeout, null));
	}

	
	/**
	 * Wait for status.
	 *
	 * @return the cluster health status
	 */
	public ClusterHealthStatus waitForStatus() {
		return waitForStatus;
	}

	
	/**
	 * Wait for status.
	 *
	 * @param waitForStatus the wait for status
	 * @return the cluster health request
	 */
	public ClusterHealthRequest waitForStatus(ClusterHealthStatus waitForStatus) {
		this.waitForStatus = waitForStatus;
		return this;
	}

	
	/**
	 * Wait for green status.
	 *
	 * @return the cluster health request
	 */
	public ClusterHealthRequest waitForGreenStatus() {
		return waitForStatus(ClusterHealthStatus.GREEN);
	}

	
	/**
	 * Wait for yellow status.
	 *
	 * @return the cluster health request
	 */
	public ClusterHealthRequest waitForYellowStatus() {
		return waitForStatus(ClusterHealthStatus.YELLOW);
	}

	
	/**
	 * Wait for relocating shards.
	 *
	 * @return the int
	 */
	public int waitForRelocatingShards() {
		return waitForRelocatingShards;
	}

	
	/**
	 * Wait for relocating shards.
	 *
	 * @param waitForRelocatingShards the wait for relocating shards
	 * @return the cluster health request
	 */
	public ClusterHealthRequest waitForRelocatingShards(int waitForRelocatingShards) {
		this.waitForRelocatingShards = waitForRelocatingShards;
		return this;
	}

	
	/**
	 * Wait for active shards.
	 *
	 * @return the int
	 */
	public int waitForActiveShards() {
		return waitForActiveShards;
	}

	
	/**
	 * Wait for active shards.
	 *
	 * @param waitForActiveShards the wait for active shards
	 * @return the cluster health request
	 */
	public ClusterHealthRequest waitForActiveShards(int waitForActiveShards) {
		this.waitForActiveShards = waitForActiveShards;
		return this;
	}

	
	/**
	 * Wait for nodes.
	 *
	 * @return the string
	 */
	public String waitForNodes() {
		return waitForNodes;
	}

	
	/**
	 * Wait for nodes.
	 *
	 * @param waitForNodes the wait for nodes
	 * @return the cluster health request
	 */
	public ClusterHealthRequest waitForNodes(String waitForNodes) {
		this.waitForNodes = waitForNodes;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		return null;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		int size = in.readVInt();
		if (size == 0) {
			indices = Strings.EMPTY_ARRAY;
		} else {
			indices = new String[size];
			for (int i = 0; i < indices.length; i++) {
				indices[i] = in.readUTF();
			}
		}
		timeout = TimeValue.readTimeValue(in);
		if (in.readBoolean()) {
			waitForStatus = ClusterHealthStatus.fromValue(in.readByte());
		}
		waitForRelocatingShards = in.readInt();
		waitForActiveShards = in.readInt();
		waitForNodes = in.readUTF();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		if (indices == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(indices.length);
			for (String index : indices) {
				out.writeUTF(index);
			}
		}
		timeout.writeTo(out);
		if (waitForStatus == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeByte(waitForStatus.value());
		}
		out.writeInt(waitForRelocatingShards);
		out.writeInt(waitForActiveShards);
		out.writeUTF(waitForNodes);
	}
}
