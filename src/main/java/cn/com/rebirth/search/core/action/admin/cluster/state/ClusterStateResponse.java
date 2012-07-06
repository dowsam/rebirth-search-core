/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterStateResponse.java 2012-3-29 15:01:07 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.state;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterState;


/**
 * The Class ClusterStateResponse.
 *
 * @author l.xue.nong
 */
public class ClusterStateResponse implements ActionResponse {

	
	/** The cluster name. */
	private ClusterName clusterName;

	
	/** The cluster state. */
	private ClusterState clusterState;

	
	/**
	 * Instantiates a new cluster state response.
	 */
	ClusterStateResponse() {
	}

	
	/**
	 * Instantiates a new cluster state response.
	 *
	 * @param clusterName the cluster name
	 * @param clusterState the cluster state
	 */
	ClusterStateResponse(ClusterName clusterName, ClusterState clusterState) {
		this.clusterName = clusterName;
		this.clusterState = clusterState;
	}

	
	/**
	 * State.
	 *
	 * @return the cluster state
	 */
	public ClusterState state() {
		return this.clusterState;
	}

	
	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public ClusterState getState() {
		return state();
	}

	
	/**
	 * Cluster name.
	 *
	 * @return the cluster name
	 */
	public ClusterName clusterName() {
		return this.clusterName;
	}

	
	/**
	 * Gets the cluster name.
	 *
	 * @return the cluster name
	 */
	public ClusterName getClusterName() {
		return clusterName();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		clusterName = ClusterName.readClusterName(in);
		clusterState = ClusterState.Builder.readFrom(in, null);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		clusterName.writeTo(out);
		ClusterState.Builder.writeTo(clusterState, out);
	}
}
