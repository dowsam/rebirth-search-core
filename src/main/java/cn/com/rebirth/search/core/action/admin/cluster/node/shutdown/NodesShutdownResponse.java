/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodesShutdownResponse.java 2012-7-6 14:28:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.shutdown;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

/**
 * The Class NodesShutdownResponse.
 *
 * @author l.xue.nong
 */
public class NodesShutdownResponse implements ActionResponse {

	/** The cluster name. */
	private ClusterName clusterName;

	/** The nodes. */
	private DiscoveryNode[] nodes;

	/**
	 * Instantiates a new nodes shutdown response.
	 */
	NodesShutdownResponse() {
	}

	/**
	 * Instantiates a new nodes shutdown response.
	 *
	 * @param clusterName the cluster name
	 * @param nodes the nodes
	 */
	public NodesShutdownResponse(ClusterName clusterName, DiscoveryNode[] nodes) {
		this.clusterName = clusterName;
		this.nodes = nodes;
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

	/**
	 * Nodes.
	 *
	 * @return the discovery node[]
	 */
	public DiscoveryNode[] nodes() {
		return this.nodes;
	}

	/**
	 * Gets the nodes.
	 *
	 * @return the nodes
	 */
	public DiscoveryNode[] getNodes() {
		return nodes();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		clusterName = ClusterName.readClusterName(in);
		nodes = new DiscoveryNode[in.readVInt()];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = DiscoveryNode.readNode(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		clusterName.writeTo(out);
		out.writeVInt(nodes.length);
		for (DiscoveryNode node : nodes) {
			node.writeTo(out);
		}
	}
}
