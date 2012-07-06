/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestClusterHealthAction.java 2012-7-6 14:29:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.cluster.health;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterHealthRequest;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterHealthResponse;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterHealthStatus;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterIndexHealth;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterShardHealth;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestActions;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestClusterHealthAction.
 *
 * @author l.xue.nong
 */
public class RestClusterHealthAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest cluster health action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestClusterHealthAction(Settings settings, Client client, RestController controller) {
		super(settings, client);

		controller.registerHandler(RestRequest.Method.GET, "/_cluster/health", this);
		controller.registerHandler(RestRequest.Method.GET, "/_cluster/health/{index}", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		ClusterHealthRequest clusterHealthRequest = Requests.clusterHealthRequest(RestActions.splitIndices(request
				.param("index")));
		int level = 0;
		try {
			clusterHealthRequest.masterNodeTimeout(request.paramAsTime("master_timeout",
					clusterHealthRequest.masterNodeTimeout()));
			clusterHealthRequest.timeout(request.paramAsTime("timeout", clusterHealthRequest.timeout()));
			String waitForStatus = request.param("wait_for_status");
			if (waitForStatus != null) {
				clusterHealthRequest.waitForStatus(ClusterHealthStatus.valueOf(waitForStatus.toUpperCase()));
			}
			clusterHealthRequest.waitForRelocatingShards(request.paramAsInt("wait_for_relocating_shards",
					clusterHealthRequest.waitForRelocatingShards()));
			clusterHealthRequest.waitForActiveShards(request.paramAsInt("wait_for_active_shards",
					clusterHealthRequest.waitForActiveShards()));
			clusterHealthRequest.waitForNodes(request.param("wait_for_nodes", clusterHealthRequest.waitForNodes()));
			String sLevel = request.param("level");
			if (sLevel != null) {
				if ("cluster".equals(sLevel)) {
					level = 0;
				} else if ("indices".equals(sLevel)) {
					level = 1;
				} else if ("shards".equals(sLevel)) {
					level = 2;
				}
			}
		} catch (Exception e) {
			try {
				XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
				channel.sendResponse(new XContentRestResponse(request, RestStatus.PRECONDITION_FAILED, builder
						.startObject().field("error", e.getMessage()).endObject()));
			} catch (IOException e1) {
				logger.error("Failed to send failure response", e1);
			}
			return;
		}
		final int fLevel = level;
		client.admin().cluster().health(clusterHealthRequest, new ActionListener<ClusterHealthResponse>() {
			@Override
			public void onResponse(ClusterHealthResponse response) {
				try {
					RestStatus status = RestStatus.OK;

					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();

					builder.field(Fields.CLUSTER_NAME, response.clusterName());
					builder.field(Fields.STATUS, response.status().name().toLowerCase());
					builder.field(Fields.TIMED_OUT, response.timedOut());
					builder.field(Fields.NUMBER_OF_NODES, response.numberOfNodes());
					builder.field(Fields.NUMBER_OF_DATA_NODES, response.numberOfDataNodes());
					builder.field(Fields.ACTIVE_PRIMARY_SHARDS, response.activePrimaryShards());
					builder.field(Fields.ACTIVE_SHARDS, response.activeShards());
					builder.field(Fields.RELOCATING_SHARDS, response.relocatingShards());
					builder.field(Fields.INITIALIZING_SHARDS, response.initializingShards());
					builder.field(Fields.UNASSIGNED_SHARDS, response.unassignedShards());

					if (!response.validationFailures().isEmpty()) {
						builder.startArray(Fields.VALIDATION_FAILURES);
						for (String validationFailure : response.validationFailures()) {
							builder.value(validationFailure);
						}

						if (fLevel == 0) {
							for (ClusterIndexHealth indexHealth : response) {
								builder.startObject(indexHealth.index());

								if (!indexHealth.validationFailures().isEmpty()) {
									builder.startArray(Fields.VALIDATION_FAILURES);
									for (String validationFailure : indexHealth.validationFailures()) {
										builder.value(validationFailure);
									}
									builder.endArray();
								}

								builder.endObject();
							}
						}
						builder.endArray();
					}

					if (fLevel > 0) {
						builder.startObject(Fields.INDICES);
						for (ClusterIndexHealth indexHealth : response) {
							builder.startObject(indexHealth.index(), XContentBuilder.FieldCaseConversion.NONE);

							builder.field(Fields.STATUS, indexHealth.status().name().toLowerCase());
							builder.field(Fields.NUMBER_OF_SHARDS, indexHealth.numberOfShards());
							builder.field(Fields.NUMBER_OF_REPLICAS, indexHealth.numberOfReplicas());
							builder.field(Fields.ACTIVE_PRIMARY_SHARDS, indexHealth.activePrimaryShards());
							builder.field(Fields.ACTIVE_SHARDS, indexHealth.activeShards());
							builder.field(Fields.RELOCATING_SHARDS, indexHealth.relocatingShards());
							builder.field(Fields.INITIALIZING_SHARDS, indexHealth.initializingShards());
							builder.field(Fields.UNASSIGNED_SHARDS, indexHealth.unassignedShards());

							if (!indexHealth.validationFailures().isEmpty()) {
								builder.startArray(Fields.VALIDATION_FAILURES);
								for (String validationFailure : indexHealth.validationFailures()) {
									builder.value(validationFailure);
								}
								builder.endArray();
							}

							if (fLevel > 1) {
								builder.startObject(Fields.SHARDS);

								for (ClusterShardHealth shardHealth : indexHealth) {
									builder.startObject(Integer.toString(shardHealth.id()));

									builder.field(Fields.STATUS, shardHealth.status().name().toLowerCase());
									builder.field(Fields.PRIMARY_ACTIVE, shardHealth.primaryActive());
									builder.field(Fields.ACTIVE_SHARDS, shardHealth.activeShards());
									builder.field(Fields.RELOCATING_SHARDS, shardHealth.relocatingShards());
									builder.field(Fields.INITIALIZING_SHARDS, shardHealth.initializingShards());
									builder.field(Fields.UNASSIGNED_SHARDS, shardHealth.unassignedShards());

									builder.endObject();
								}

								builder.endObject();
							}

							builder.endObject();
						}
						builder.endObject();
					}

					builder.endObject();

					channel.sendResponse(new XContentRestResponse(request, status, builder));
				} catch (Exception e) {
					onFailure(e);
				}
			}

			@Override
			public void onFailure(Throwable e) {
				try {
					channel.sendResponse(new XContentThrowableRestResponse(request, e));
				} catch (IOException e1) {
					logger.error("Failed to send failure response", e1);
				}
			}
		});
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant CLUSTER_NAME. */
		static final XContentBuilderString CLUSTER_NAME = new XContentBuilderString("cluster_name");

		/** The Constant STATUS. */
		static final XContentBuilderString STATUS = new XContentBuilderString("status");

		/** The Constant TIMED_OUT. */
		static final XContentBuilderString TIMED_OUT = new XContentBuilderString("timed_out");

		/** The Constant NUMBER_OF_SHARDS. */
		static final XContentBuilderString NUMBER_OF_SHARDS = new XContentBuilderString("number_of_shards");

		/** The Constant NUMBER_OF_REPLICAS. */
		static final XContentBuilderString NUMBER_OF_REPLICAS = new XContentBuilderString("number_of_replicas");

		/** The Constant NUMBER_OF_NODES. */
		static final XContentBuilderString NUMBER_OF_NODES = new XContentBuilderString("number_of_nodes");

		/** The Constant NUMBER_OF_DATA_NODES. */
		static final XContentBuilderString NUMBER_OF_DATA_NODES = new XContentBuilderString("number_of_data_nodes");

		/** The Constant ACTIVE_PRIMARY_SHARDS. */
		static final XContentBuilderString ACTIVE_PRIMARY_SHARDS = new XContentBuilderString("active_primary_shards");

		/** The Constant ACTIVE_SHARDS. */
		static final XContentBuilderString ACTIVE_SHARDS = new XContentBuilderString("active_shards");

		/** The Constant RELOCATING_SHARDS. */
		static final XContentBuilderString RELOCATING_SHARDS = new XContentBuilderString("relocating_shards");

		/** The Constant INITIALIZING_SHARDS. */
		static final XContentBuilderString INITIALIZING_SHARDS = new XContentBuilderString("initializing_shards");

		/** The Constant UNASSIGNED_SHARDS. */
		static final XContentBuilderString UNASSIGNED_SHARDS = new XContentBuilderString("unassigned_shards");

		/** The Constant VALIDATION_FAILURES. */
		static final XContentBuilderString VALIDATION_FAILURES = new XContentBuilderString("validation_failures");

		/** The Constant INDICES. */
		static final XContentBuilderString INDICES = new XContentBuilderString("indices");

		/** The Constant SHARDS. */
		static final XContentBuilderString SHARDS = new XContentBuilderString("shards");

		/** The Constant PRIMARY_ACTIVE. */
		static final XContentBuilderString PRIMARY_ACTIVE = new XContentBuilderString("primary_active");
	}
}
