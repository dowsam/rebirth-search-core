/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestClusterStateAction.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.cluster.state;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.compress.CompressedString;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.settings.SettingsFilter;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateRequest;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlock;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.IndexTemplateMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MappingMetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.routing.IndexRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.IndexShardRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.AllocationExplanation;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestActions;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

import com.google.common.collect.ImmutableSet;

/**
 * The Class RestClusterStateAction.
 *
 * @author l.xue.nong
 */
public class RestClusterStateAction extends BaseRestHandler {

	/** The settings filter. */
	private final SettingsFilter settingsFilter;

	/**
	 * Instantiates a new rest cluster state action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 * @param settingsFilter the settings filter
	 */
	@Inject
	public RestClusterStateAction(Settings settings, Client client, RestController controller,
			SettingsFilter settingsFilter) {
		super(settings, client);
		controller.registerHandler(RestRequest.Method.GET, "/_cluster/state", this);

		this.settingsFilter = settingsFilter;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		final ClusterStateRequest clusterStateRequest = Requests.clusterStateRequest();
		clusterStateRequest.masterNodeTimeout(request.paramAsTime("master_timeout",
				clusterStateRequest.masterNodeTimeout()));
		clusterStateRequest.filterNodes(request.paramAsBoolean("filter_nodes", clusterStateRequest.filterNodes()));
		clusterStateRequest.filterRoutingTable(request.paramAsBoolean("filter_routing_table",
				clusterStateRequest.filterRoutingTable()));
		clusterStateRequest.filterMetaData(request.paramAsBoolean("filter_metadata",
				clusterStateRequest.filterMetaData()));
		clusterStateRequest.filterBlocks(request.paramAsBoolean("filter_blocks", clusterStateRequest.filterBlocks()));
		clusterStateRequest.filteredIndices(RestActions.splitIndices(request.param("filter_indices", null)));
		clusterStateRequest.filteredIndexTemplates(request.paramAsStringArray("filter_index_templates",
				Strings.EMPTY_ARRAY));
		clusterStateRequest.local(request.paramAsBoolean("local", clusterStateRequest.local()));
		client.admin().cluster().state(clusterStateRequest, new ActionListener<ClusterStateResponse>() {
			@Override
			public void onResponse(ClusterStateResponse response) {
				try {
					ClusterState state = response.state();
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();

					builder.field("cluster_name", response.clusterName().value());

					if (!clusterStateRequest.filterNodes()) {
						builder.field("master_node", state.nodes().masterNodeId());
					}

					if (!clusterStateRequest.filterBlocks()) {
						builder.startObject("blocks");

						if (!state.blocks().global().isEmpty()) {
							builder.startObject("global");
							for (ClusterBlock block : state.blocks().global()) {
								block.toXContent(builder, request);
							}
							builder.endObject();
						}

						if (!state.blocks().indices().isEmpty()) {
							builder.startObject("indices");
							for (Map.Entry<String, ImmutableSet<ClusterBlock>> entry : state.blocks().indices()
									.entrySet()) {
								builder.startObject(entry.getKey());
								for (ClusterBlock block : entry.getValue()) {
									block.toXContent(builder, request);
								}
								builder.endObject();
							}
							builder.endObject();
						}

						builder.endObject();
					}

					if (!clusterStateRequest.filterNodes()) {
						builder.startObject("nodes");
						for (DiscoveryNode node : state.nodes()) {
							builder.startObject(node.id(), XContentBuilder.FieldCaseConversion.NONE);
							builder.field("name", node.name());
							builder.field("transport_address", node.address().toString());

							builder.startObject("attributes");
							for (Map.Entry<String, String> attr : node.attributes().entrySet()) {
								builder.field(attr.getKey(), attr.getValue());
							}
							builder.endObject();

							builder.endObject();
						}
						builder.endObject();
					}

					if (!clusterStateRequest.filterMetaData()) {
						builder.startObject("metadata");

						builder.startObject("templates");
						for (IndexTemplateMetaData templateMetaData : state.metaData().templates().values()) {
							builder.startObject(templateMetaData.name(), XContentBuilder.FieldCaseConversion.NONE);

							builder.field("template", templateMetaData.template());
							builder.field("order", templateMetaData.order());

							builder.startObject("settings");
							Settings settings = settingsFilter.filterSettings(templateMetaData.settings());
							for (Map.Entry<String, String> entry : settings.getAsMap().entrySet()) {
								builder.field(entry.getKey(), entry.getValue());
							}
							builder.endObject();

							builder.startObject("mappings");
							for (Map.Entry<String, CompressedString> entry : templateMetaData.mappings().entrySet()) {
								byte[] mappingSource = entry.getValue().uncompressed();
								XContentParser parser = XContentFactory.xContent(mappingSource).createParser(
										mappingSource);
								Map<String, Object> mapping = parser.map();
								if (mapping.size() == 1 && mapping.containsKey(entry.getKey())) {

									mapping = (Map<String, Object>) mapping.get(entry.getKey());
								}
								builder.field(entry.getKey());
								builder.map(mapping);
							}
							builder.endObject();

							builder.endObject();
						}
						builder.endObject();

						builder.startObject("indices");
						for (IndexMetaData indexMetaData : state.metaData()) {
							builder.startObject(indexMetaData.index(), XContentBuilder.FieldCaseConversion.NONE);

							builder.field("state", indexMetaData.state().toString().toLowerCase());

							builder.startObject("settings");
							Settings settings = settingsFilter.filterSettings(indexMetaData.settings());
							for (Map.Entry<String, String> entry : settings.getAsMap().entrySet()) {
								builder.field(entry.getKey(), entry.getValue());
							}
							builder.endObject();

							builder.startObject("mappings");
							for (Map.Entry<String, MappingMetaData> entry : indexMetaData.mappings().entrySet()) {
								byte[] mappingSource = entry.getValue().source().uncompressed();
								XContentParser parser = XContentFactory.xContent(mappingSource).createParser(
										mappingSource);
								Map<String, Object> mapping = parser.map();
								if (mapping.size() == 1 && mapping.containsKey(entry.getKey())) {

									mapping = (Map<String, Object>) mapping.get(entry.getKey());
								}
								builder.field(entry.getKey());
								builder.map(mapping);
							}
							builder.endObject();

							builder.startArray("aliases");
							for (String alias : indexMetaData.aliases().keySet()) {
								builder.value(alias);
							}
							builder.endArray();

							builder.endObject();
						}
						builder.endObject();

						builder.endObject();
					}

					if (!clusterStateRequest.filterRoutingTable()) {
						builder.startObject("routing_table");
						builder.startObject("indices");
						for (IndexRoutingTable indexRoutingTable : state.routingTable()) {
							builder.startObject(indexRoutingTable.index(), XContentBuilder.FieldCaseConversion.NONE);
							builder.startObject("shards");
							for (IndexShardRoutingTable indexShardRoutingTable : indexRoutingTable) {
								builder.startArray(Integer.toString(indexShardRoutingTable.shardId().id()));
								for (ShardRouting shardRouting : indexShardRoutingTable) {
									jsonShardRouting(builder, shardRouting);
								}
								builder.endArray();
							}
							builder.endObject();
							builder.endObject();
						}
						builder.endObject();
						builder.endObject();
					}

					if (!clusterStateRequest.filterRoutingTable()) {
						builder.startObject("routing_nodes");
						builder.startArray("unassigned");
						for (ShardRouting shardRouting : state.readOnlyRoutingNodes().unassigned()) {
							jsonShardRouting(builder, shardRouting);
						}
						builder.endArray();

						builder.startObject("nodes");
						for (RoutingNode routingNode : state.readOnlyRoutingNodes()) {
							builder.startArray(routingNode.nodeId(), XContentBuilder.FieldCaseConversion.NONE);
							for (ShardRouting shardRouting : routingNode) {
								jsonShardRouting(builder, shardRouting);
							}
							builder.endArray();
						}
						builder.endObject();

						builder.endObject();
					}

					if (!clusterStateRequest.filterRoutingTable()) {
						builder.startArray("allocations");
						for (Map.Entry<ShardId, List<AllocationExplanation.NodeExplanation>> entry : state
								.allocationExplanation().explanations().entrySet()) {
							builder.startObject();
							builder.field("index", entry.getKey().index().name());
							builder.field("shard", entry.getKey().id());
							builder.startArray("explanations");
							for (AllocationExplanation.NodeExplanation nodeExplanation : entry.getValue()) {
								builder.field("desc", nodeExplanation.description());
								if (nodeExplanation.node() != null) {
									builder.startObject("node");
									builder.field("id", nodeExplanation.node().id());
									builder.field("name", nodeExplanation.node().name());
									builder.endObject();
								}
							}
							builder.endArray();
							builder.endObject();
						}
						builder.endArray();
					}

					builder.endObject();
					channel.sendResponse(new XContentRestResponse(request, RestStatus.OK, builder));
				} catch (Exception e) {
					onFailure(e);
				}
			}

			private void jsonShardRouting(XContentBuilder builder, ShardRouting shardRouting) throws IOException {
				builder.startObject().field("state", shardRouting.state()).field("primary", shardRouting.primary())
						.field("node", shardRouting.currentNodeId())
						.field("relocating_node", shardRouting.relocatingNodeId())
						.field("shard", shardRouting.shardId().id())
						.field("index", shardRouting.shardId().index().name()).endObject();
			}

			@Override
			public void onFailure(Throwable e) {
				if (logger.isDebugEnabled()) {
					logger.debug("failed to handle cluster state", e);
				}
				try {
					channel.sendResponse(new XContentThrowableRestResponse(request, e));
				} catch (IOException e1) {
					logger.error("Failed to send failure response", e1);
				}
			}
		});
	}
}