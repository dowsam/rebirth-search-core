/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestNodesInfoAction.java 2012-3-29 15:01:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest.action.admin.cluster.node.info;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.settings.SettingsFilter;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodesInfoRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodesInfoResponse;
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
 * The Class RestNodesInfoAction.
 *
 * @author l.xue.nong
 */
public class RestNodesInfoAction extends BaseRestHandler {

	
	/** The settings filter. */
	private final SettingsFilter settingsFilter;

	
	/**
	 * Instantiates a new rest nodes info action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 * @param settingsFilter the settings filter
	 */
	@Inject
	public RestNodesInfoAction(Settings settings, Client client, RestController controller,
			SettingsFilter settingsFilter) {
		super(settings, client);
		controller.registerHandler(RestRequest.Method.GET, "/_cluster/nodes", this);
		controller.registerHandler(RestRequest.Method.GET, "/_cluster/nodes/{nodeId}", this);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes", this);
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}", this);

		controller.registerHandler(RestRequest.Method.GET, "/_nodes/settings", new RestSettingsHandler());
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/settings", new RestSettingsHandler());

		controller.registerHandler(RestRequest.Method.GET, "/_nodes/os", new RestOsHandler());
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/os", new RestOsHandler());

		controller.registerHandler(RestRequest.Method.GET, "/_nodes/process", new RestProcessHandler());
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/process", new RestProcessHandler());

		controller.registerHandler(RestRequest.Method.GET, "/_nodes/jvm", new RestJvmHandler());
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/jvm", new RestJvmHandler());

		controller.registerHandler(RestRequest.Method.GET, "/_nodes/thread_pool", new RestThreadPoolHandler());
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/thread_pool", new RestThreadPoolHandler());

		controller.registerHandler(RestRequest.Method.GET, "/_nodes/network", new RestNetworkHandler());
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/network", new RestNetworkHandler());

		controller.registerHandler(RestRequest.Method.GET, "/_nodes/transport", new RestTransportHandler());
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/transport", new RestTransportHandler());

		controller.registerHandler(RestRequest.Method.GET, "/_nodes/http", new RestHttpHandler());
		controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/http", new RestHttpHandler());

		this.settingsFilter = settingsFilter;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		String[] nodesIds = RestActions.splitNodes(request.param("nodeId"));
		final NodesInfoRequest nodesInfoRequest = new NodesInfoRequest(nodesIds);

		boolean clear = request.paramAsBoolean("clear", false);
		if (clear) {
			nodesInfoRequest.clear();
		}
		boolean all = request.paramAsBoolean("all", false);
		if (all) {
			nodesInfoRequest.all();
		}
		nodesInfoRequest.settings(request.paramAsBoolean("settings", nodesInfoRequest.settings()));
		nodesInfoRequest.os(request.paramAsBoolean("os", nodesInfoRequest.os()));
		nodesInfoRequest.process(request.paramAsBoolean("process", nodesInfoRequest.process()));
		nodesInfoRequest.jvm(request.paramAsBoolean("jvm", nodesInfoRequest.jvm()));
		nodesInfoRequest.threadPool(request.paramAsBoolean("thread_pool", nodesInfoRequest.threadPool()));
		nodesInfoRequest.network(request.paramAsBoolean("network", nodesInfoRequest.network()));
		nodesInfoRequest.transport(request.paramAsBoolean("transport", nodesInfoRequest.transport()));
		nodesInfoRequest.http(request.paramAsBoolean("http", nodesInfoRequest.http()));

		executeNodeRequest(request, channel, nodesInfoRequest);
	}

	
	/**
	 * Execute node request.
	 *
	 * @param request the request
	 * @param channel the channel
	 * @param nodesInfoRequest the nodes info request
	 */
	void executeNodeRequest(final RestRequest request, final RestChannel channel, NodesInfoRequest nodesInfoRequest) {
		nodesInfoRequest.listenerThreaded(false);
		client.admin().cluster().nodesInfo(nodesInfoRequest, new ActionListener<NodesInfoResponse>() {
			@Override
			public void onResponse(NodesInfoResponse response) {
				try {
					response.settingsFilter(settingsFilter);
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();
					builder.field("ok", true);
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
	 * The Class RestSettingsHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestSettingsHandler implements RestHandler {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesInfoRequest nodesInfoRequest = new NodesInfoRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesInfoRequest.clear().settings(true);
			executeNodeRequest(request, channel, nodesInfoRequest);
		}
	}

	
	/**
	 * The Class RestOsHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestOsHandler implements RestHandler {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesInfoRequest nodesInfoRequest = new NodesInfoRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesInfoRequest.clear().os(true);
			executeNodeRequest(request, channel, nodesInfoRequest);
		}
	}

	
	/**
	 * The Class RestProcessHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestProcessHandler implements RestHandler {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesInfoRequest nodesInfoRequest = new NodesInfoRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesInfoRequest.clear().process(true);
			executeNodeRequest(request, channel, nodesInfoRequest);
		}
	}

	
	/**
	 * The Class RestJvmHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestJvmHandler implements RestHandler {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesInfoRequest nodesInfoRequest = new NodesInfoRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesInfoRequest.clear().jvm(true);
			executeNodeRequest(request, channel, nodesInfoRequest);
		}
	}

	
	/**
	 * The Class RestThreadPoolHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestThreadPoolHandler implements RestHandler {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesInfoRequest nodesInfoRequest = new NodesInfoRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesInfoRequest.clear().threadPool(true);
			executeNodeRequest(request, channel, nodesInfoRequest);
		}
	}

	
	/**
	 * The Class RestNetworkHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestNetworkHandler implements RestHandler {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesInfoRequest nodesInfoRequest = new NodesInfoRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesInfoRequest.clear().network(true);
			executeNodeRequest(request, channel, nodesInfoRequest);
		}
	}

	
	/**
	 * The Class RestTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestTransportHandler implements RestHandler {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesInfoRequest nodesInfoRequest = new NodesInfoRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesInfoRequest.clear().transport(true);
			executeNodeRequest(request, channel, nodesInfoRequest);
		}
	}

	
	/**
	 * The Class RestHttpHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestHttpHandler implements RestHandler {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			NodesInfoRequest nodesInfoRequest = new NodesInfoRequest(RestActions.splitNodes(request.param("nodeId")));
			nodesInfoRequest.clear().http(true);
			executeNodeRequest(request, channel, nodesInfoRequest);
		}
	}
}
