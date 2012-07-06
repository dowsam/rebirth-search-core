/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodesStatsResponse.java 2012-7-6 14:29:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.stats;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.support.nodes.NodesOperationResponse;
import cn.com.rebirth.search.core.cluster.ClusterName;

/**
 * The Class NodesStatsResponse.
 *
 * @author l.xue.nong
 */
public class NodesStatsResponse extends NodesOperationResponse<NodeStats> implements ToXContent {

	/**
	 * Instantiates a new nodes stats response.
	 */
	NodesStatsResponse() {
	}

	/**
	 * Instantiates a new nodes stats response.
	 *
	 * @param clusterName the cluster name
	 * @param nodes the nodes
	 */
	public NodesStatsResponse(ClusterName clusterName, NodeStats[] nodes) {
		super(clusterName, nodes);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.NodesOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		nodes = new NodeStats[in.readVInt()];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = NodeStats.readNodeStats(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.NodesOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeVInt(nodes.length);
		for (NodeStats node : nodes) {
			node.writeTo(out);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.field("cluster_name", clusterName().value());

		builder.startObject("nodes");
		for (NodeStats nodeStats : this) {
			builder.startObject(nodeStats.node().id(), XContentBuilder.FieldCaseConversion.NONE);

			builder.field("name", nodeStats.node().name(), XContentBuilder.FieldCaseConversion.NONE);
			builder.field("transport_address", nodeStats.node().address().toString());

			if (nodeStats.hostname() != null) {
				builder.field("hostname", nodeStats.hostname(), XContentBuilder.FieldCaseConversion.NONE);
			}

			if (!nodeStats.node().attributes().isEmpty()) {
				builder.startObject("attributes");
				for (Map.Entry<String, String> attr : nodeStats.node().attributes().entrySet()) {
					builder.field(attr.getKey(), attr.getValue());
				}
				builder.endObject();
			}

			if (nodeStats.indices() != null) {
				nodeStats.indices().toXContent(builder, params);
			}

			if (nodeStats.os() != null) {
				nodeStats.os().toXContent(builder, params);
			}
			if (nodeStats.process() != null) {
				nodeStats.process().toXContent(builder, params);
			}
			if (nodeStats.jvm() != null) {
				nodeStats.jvm().toXContent(builder, params);
			}
			if (nodeStats.threadPool() != null) {
				nodeStats.threadPool().toXContent(builder, params);
			}
			if (nodeStats.network() != null) {
				nodeStats.network().toXContent(builder, params);
			}
			if (nodeStats.fs() != null) {
				nodeStats.fs().toXContent(builder, params);
			}
			if (nodeStats.transport() != null) {
				nodeStats.transport().toXContent(builder, params);
			}
			if (nodeStats.http() != null) {
				nodeStats.http().toXContent(builder, params);
			}
			builder.endObject();
		}
		builder.endObject();

		return builder;
	}
}