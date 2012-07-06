/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestNodesShutdownAction.java 2012-7-6 14:29:03 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.cluster.node.shutdown;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.node.shutdown.NodesShutdownRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.shutdown.NodesShutdownResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
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
 * The Class RestNodesShutdownAction.
 *
 * @author l.xue.nong
 */
public class RestNodesShutdownAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest nodes shutdown action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestNodesShutdownAction(Settings settings, Client client, RestController controller) {
		super(settings, client);

		controller.registerHandler(RestRequest.Method.POST, "/_shutdown", this);
		controller.registerHandler(RestRequest.Method.POST, "/_cluster/nodes/_shutdown", this);
		controller.registerHandler(RestRequest.Method.POST, "/_cluster/nodes/{nodeId}/_shutdown", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		String[] nodesIds = RestActions.splitNodes(request.param("nodeId"));
		NodesShutdownRequest nodesShutdownRequest = new NodesShutdownRequest(nodesIds);
		nodesShutdownRequest.listenerThreaded(false);
		nodesShutdownRequest.delay(request.paramAsTime("delay", nodesShutdownRequest.delay()));
		nodesShutdownRequest.exit(request.paramAsBoolean("exit", nodesShutdownRequest.exit()));
		client.admin().cluster().nodesShutdown(nodesShutdownRequest, new ActionListener<NodesShutdownResponse>() {
			@Override
			public void onResponse(NodesShutdownResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();
					builder.field("cluster_name", response.clusterName().value());

					builder.startObject("nodes");
					for (DiscoveryNode node : response.nodes()) {
						builder.startObject(node.id(), XContentBuilder.FieldCaseConversion.NONE);
						builder.field("name", node.name(), XContentBuilder.FieldCaseConversion.NONE);
						builder.endObject();
					}
					builder.endObject();

					builder.endObject();
					channel.sendResponse(new XContentRestResponse(request, RestStatus.OK, builder));
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
}