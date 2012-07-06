/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodesInfoResponse.java 2012-3-29 15:02:15 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.info;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.settings.SettingsFilter;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.support.nodes.NodesOperationResponse;
import cn.com.rebirth.search.core.cluster.ClusterName;

/**
 * The Class NodesInfoResponse.
 *
 * @author l.xue.nong
 */
public class NodesInfoResponse extends NodesOperationResponse<NodeInfo> implements ToXContent {

	/** The settings filter. */
	private SettingsFilter settingsFilter;

	/**
	 * Instantiates a new nodes info response.
	 */
	public NodesInfoResponse() {
	}

	/**
	 * Instantiates a new nodes info response.
	 *
	 * @param clusterName the cluster name
	 * @param nodes the nodes
	 */
	public NodesInfoResponse(ClusterName clusterName, NodeInfo[] nodes) {
		super(clusterName, nodes);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.nodes.NodesOperationResponse#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		nodes = new NodeInfo[in.readVInt()];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = NodeInfo.readNodeInfo(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.nodes.NodesOperationResponse#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeVInt(nodes.length);
		for (NodeInfo node : nodes) {
			node.writeTo(out);
		}
	}

	/**
	 * Settings filter.
	 *
	 * @param settingsFilter the settings filter
	 * @return the nodes info response
	 */
	public NodesInfoResponse settingsFilter(SettingsFilter settingsFilter) {
		this.settingsFilter = settingsFilter;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.field("cluster_name", clusterName().value());

		builder.startObject("nodes");
		for (NodeInfo nodeInfo : this) {
			builder.startObject(nodeInfo.node().id(), XContentBuilder.FieldCaseConversion.NONE);

			builder.field("name", nodeInfo.node().name(), XContentBuilder.FieldCaseConversion.NONE);
			builder.field("transport_address", nodeInfo.node().address().toString());

			if (nodeInfo.hostname() != null) {
				builder.field("hostname", nodeInfo.hostname(), XContentBuilder.FieldCaseConversion.NONE);
			}

			if (nodeInfo.serviceAttributes() != null) {
				for (Map.Entry<String, String> nodeAttribute : nodeInfo.serviceAttributes().entrySet()) {
					builder.field(nodeAttribute.getKey(), nodeAttribute.getValue());
				}
			}

			if (!nodeInfo.node().attributes().isEmpty()) {
				builder.startObject("attributes");
				for (Map.Entry<String, String> attr : nodeInfo.node().attributes().entrySet()) {
					builder.field(attr.getKey(), attr.getValue());
				}
				builder.endObject();
			}

			if (nodeInfo.settings() != null) {
				builder.startObject("settings");
				Settings settings = settingsFilter.filterSettings(nodeInfo.settings());
				for (Map.Entry<String, String> entry : settings.getAsMap().entrySet()) {
					builder.field(entry.getKey(), entry.getValue());
				}
				builder.endObject();
			}

			if (nodeInfo.os() != null) {
				nodeInfo.os().toXContent(builder, params);
			}
			if (nodeInfo.process() != null) {
				nodeInfo.process().toXContent(builder, params);
			}
			if (nodeInfo.jvm() != null) {
				nodeInfo.jvm().toXContent(builder, params);
			}
			if (nodeInfo.threadPool() != null) {
				nodeInfo.threadPool().toXContent(builder, params);
			}
			if (nodeInfo.network() != null) {
				nodeInfo.network().toXContent(builder, params);
			}
			if (nodeInfo.transport() != null) {
				nodeInfo.transport().toXContent(builder, params);
			}
			if (nodeInfo.http() != null) {
				nodeInfo.http().toXContent(builder, params);
			}
			builder.endObject();
		}
		builder.endObject();
		return builder;
	}
}
