/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestNodesStatsAction.java 2012-7-6 14:29:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.cluster.node.stats;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.node.stats.NodesStatsRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.stats.NodesStatsResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestHandler;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestActions;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestNodesStatsAction.
 *
 * @author l.xue.nong
 */
public class RestNodesStatsAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest nodes stats action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestNodesStatsAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(RestRequest.Method.GET, "/_cluster/nodes/stats", this);
		controller.registerHandler(RestRequest.Method.GET, "/_cluster/nodes/{nodeId}/stats", this);

		controller.registerHandler(RestRequest.Method.GET, "/_nodes/stats", this);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/stats", this);

		RestIndicesHandler indicesHandler = new RestIndicesHandler();
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/stats/indices", indicesHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/stats/indices", indicesHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/indices/stats", indicesHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/indices/stats", indicesHandler);

		RestOsHandler osHandler = new RestOsHandler();
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/stats/os", osHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/stats/os", osHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/os/stats", osHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/os/stats", osHandler);

		RestProcessHandler processHandler = new RestProcessHandler();
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/stats/process", processHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/stats/process", processHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/process/stats", processHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/process/stats", processHandler);

		RestJvmHandler jvmHandler = new RestJvmHandler();
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/stats/jvm", jvmHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/stats/jvm", jvmHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/jvm/stats", jvmHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/jvm/stats", jvmHandler);

		RestThreadPoolHandler threadPoolHandler = new RestThreadPoolHandler();
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/stats/thread_pool", threadPoolHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/stats/thread_pool", threadPoolHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/thread_pool/stats", threadPoolHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/thread_pool/stats", threadPoolHandler);

		RestNetworkHandler networkHandler = new RestNetworkHandler();
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/stats/network", networkHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/stats/network", networkHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/network/stats", networkHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/network/stats", networkHandler);

		RestFsHandler fsHandler = new RestFsHandler();
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/stats/fs", fsHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/stats/fs", fsHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/fs/stats", fsHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/fs/stats", fsHandler);

		RestTransportHandler transportHandler = new RestTransportHandler();
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/stats/transport", transportHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/stats/transport", transportHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/transport/stats", transportHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/transport/stats", transportHandler);

		RestHttpHandler httpHandler = new RestHttpHandler();
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/stats/http", httpHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/stats/http", httpHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/http/stats", httpHandler);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/http/stats", httpHandler);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		String[] nodesIds = RestActions.splitNodes(request.param("nodeId"));
		NodesStatsRequest nodesStatsRequest = new NodesStatsRequest(nodesIds);
		boolean clear = request.paramAsBoolean("clear", false);
		if (clear) {
			nodesStatsRequest.clear();
		}
		boolean all = request.paramAsBoolean("all", false);
		if (all) {
			nodesStatsRequest.all();
		}
		nodesStatsRequest.indices(request.paramAsBoolean("indices", nodesStatsRequest.indices()));
		nodesStatsRequest.os(request.paramAsBoolean("os", nodesStatsRequest.os()));
		nodesStatsRequest.process(request.paramAsBoolean("process", nodesStatsRequest.process()));
		nodesStatsRequest.jvm(request.paramAsBoolean("jvm", nodesStatsRequest.jvm()));
		nodesStatsRequest.threadPool(request.paramAsBoolean("thread_pool", nodesStatsRequest.threadPool()));
		nodesStatsRequest.network(request.paramAsBoolean("network", nodesStatsRequest.network()));
		nodesStatsRequest.fs(request.paramAsBoolean("fs", nodesStatsRequest.fs()));
		nodesStatsRequest.transport(request.paramAsBoolean("transport", nodesStatsRequest.transport()));
		nodesStatsRequest.http(request.paramAsBoolean("http", nodesStatsRequest.http()));
		executeNodeStats(request, channel, nodesStatsRequest);
	}

	/**
	 * Execute node stats.
	 *
	 * @param request the request
	 * @param channel the channel
	 * @param nodesStatsRequest the nodes stats request
	 */
	void executeNodeStats(final RestRequest request, final RestChannel channel,
			final NodesStatsRequest nodesStatsRequest) {
		nodesStatsRequest.listenerThreaded(false);
		client.admin().cluster().nodesStats(nodesStatsRequest, new ActionListener<NodesStatsResponse>() {
			@Override
			public void onResponse(NodesStatsResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();
					response.toXContent(builder, request);
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

	/**
	 * The Class RestIndicesHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestIndicesHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesStatsRequest nodesStatsRequest = new NodesStatsRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesStatsRequest.clear().indices(true);
			executeNodeStats(request, channel, nodesStatsRequest);
		}
	}

	/**
	 * The Class RestOsHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestOsHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesStatsRequest nodesStatsRequest = new NodesStatsRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesStatsRequest.clear().os(true);
			executeNodeStats(request, channel, nodesStatsRequest);
		}
	}

	/**
	 * The Class RestProcessHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestProcessHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesStatsRequest nodesStatsRequest = new NodesStatsRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesStatsRequest.clear().process(true);
			executeNodeStats(request, channel, nodesStatsRequest);
		}
	}

	/**
	 * The Class RestJvmHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestJvmHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesStatsRequest nodesStatsRequest = new NodesStatsRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesStatsRequest.clear().jvm(true);
			executeNodeStats(request, channel, nodesStatsRequest);
		}
	}

	/**
	 * The Class RestThreadPoolHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestThreadPoolHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesStatsRequest nodesStatsRequest = new NodesStatsRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesStatsRequest.clear().threadPool(true);
			executeNodeStats(request, channel, nodesStatsRequest);
		}
	}

	/**
	 * The Class RestNetworkHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestNetworkHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesStatsRequest nodesStatsRequest = new NodesStatsRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesStatsRequest.clear().network(true);
			executeNodeStats(request, channel, nodesStatsRequest);
		}
	}

	/**
	 * The Class RestFsHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestFsHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesStatsRequest nodesStatsRequest = new NodesStatsRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesStatsRequest.clear().fs(true);
			executeNodeStats(request, channel, nodesStatsRequest);
		}
	}

	/**
	 * The Class RestTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestTransportHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesStatsRequest nodesStatsRequest = new NodesStatsRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesStatsRequest.clear().transport(true);
			executeNodeStats(request, channel, nodesStatsRequest);
		}
	}

	/**
	 * The Class RestHttpHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestHttpHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesStatsRequest nodesStatsRequest = new NodesStatsRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesStatsRequest.clear().http(true);
			executeNodeStats(request, channel, nodesStatsRequest);
		}
	}
}