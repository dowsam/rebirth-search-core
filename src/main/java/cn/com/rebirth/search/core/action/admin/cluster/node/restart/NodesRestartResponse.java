/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodesRestartResponse.java 2012-7-6 14:30:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.restart;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.nodes.NodeOperationResponse;
import cn.com.rebirth.search.core.action.support.nodes.NodesOperationResponse;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

/**
 * The Class NodesRestartResponse.
 *
 * @author l.xue.nong
 */
public class NodesRestartResponse extends NodesOperationResponse<NodesRestartResponse.NodeRestartResponse> {

	/**
	 * Instantiates a new nodes restart response.
	 */
	NodesRestartResponse() {
	}

	/**
	 * Instantiates a new nodes restart response.
	 *
	 * @param clusterName the cluster name
	 * @param nodes the nodes
	 */
	public NodesRestartResponse(ClusterName clusterName, NodeRestartResponse[] nodes) {
		super(clusterName, nodes);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.NodesOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		nodes = new NodeRestartResponse[in.readVInt()];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = NodeRestartResponse.readNodeRestartResponse(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.NodesOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeVInt(nodes.length);
		for (NodeRestartResponse node : nodes) {
			node.writeTo(out);
		}
	}

	/**
	 * The Class NodeRestartResponse.
	 *
	 * @author l.xue.nong
	 */
	public static class NodeRestartResponse extends NodeOperationResponse {

		/**
		 * Instantiates a new node restart response.
		 */
		NodeRestartResponse() {
		}

		/**
		 * Instantiates a new node restart response.
		 *
		 * @param node the node
		 */
		public NodeRestartResponse(DiscoveryNode node) {
			super(node);
		}

		/**
		 * Read node restart response.
		 *
		 * @param in the in
		 * @return the node restart response
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static NodeRestartResponse readNodeRestartResponse(StreamInput in) throws IOException {
			NodeRestartResponse res = new NodeRestartResponse();
			res.readFrom(in);
			return res;
		}
	}
}