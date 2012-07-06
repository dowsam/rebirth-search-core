/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestNodesRestartAction.java 2012-7-6 14:30:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.cluster.node.restart;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.node.restart.NodesRestartRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.restart.NodesRestartResponse;
import cn.com.rebirth.search.core.client.Client;
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
 * The Class RestNodesRestartAction.
 *
 * @author l.xue.nong
 */
public class RestNodesRestartAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest nodes restart action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestNodesRestartAction(Settings settings, Client client, RestController controller) {
		super(settings, client);

		controller.registerHandler(RestRequest.Method.POST, "/_cluster/nodes/_restart", this);
		controller.registerHandler(RestRequest.Method.POST, "/_cluster/nodes/{nodeId}/_restart", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		String[] nodesIds = RestActions.splitNodes(request.param("nodeId"));
		NodesRestartRequest nodesRestartRequest = new NodesRestartRequest(nodesIds);
		nodesRestartRequest.listenerThreaded(false);
		nodesRestartRequest.delay(request.paramAsTime("delay", nodesRestartRequest.delay()));
		client.admin().cluster().nodesRestart(nodesRestartRequest, new ActionListener<NodesRestartResponse>() {
			@Override
			public void onResponse(NodesRestartResponse result) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();
					builder.field("cluster_name", result.clusterName().value());

					builder.startObject("nodes");
					for (NodesRestartResponse.NodeRestartResponse nodeInfo : result) {
						builder.startObject(nodeInfo.node().id());
						builder.field("name", nodeInfo.node().name());
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