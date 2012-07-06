/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestMainAction.java 2012-7-6 14:30:29 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.main;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.RestartSearchCoreVersion;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateRequest;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.StringRestResponse;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.RestRequest.Method;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestMainAction.
 *
 * @author l.xue.nong
 */
public class RestMainAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest main action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestMainAction(Settings settings, Client client, RestController controller) {
		super(settings, client);

		controller.registerHandler(Method.GET, "/", this);
		controller.registerHandler(Method.HEAD, "/", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		ClusterStateRequest clusterStateRequest = new ClusterStateRequest();
		clusterStateRequest.masterNodeTimeout(TimeValue.timeValueMillis(0));
		clusterStateRequest.local(true);
		clusterStateRequest.filterAll().filterBlocks(false);
		client.admin().cluster().state(clusterStateRequest, new ActionListener<ClusterStateResponse>() {
			@Override
			public void onResponse(ClusterStateResponse response) {
				RestStatus status = RestStatus.OK;
				if (response.state().blocks().hasGlobalBlock(RestStatus.SERVICE_UNAVAILABLE)) {
					status = RestStatus.SERVICE_UNAVAILABLE;
				}
				if (request.method() == RestRequest.Method.HEAD) {
					channel.sendResponse(new StringRestResponse(status));
					return;
				}

				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request).prettyPrint();
					builder.startObject();
					builder.field("ok", true);
					builder.field("status", status.getStatus());
					if (settings.get("name") != null) {
						builder.field("name", settings.get("name"));
					}
					builder.startObject("version").field("number", new RestartSearchCoreVersion().getModuleVersion())
							.field("snapshot_build", new RestartSearchCoreVersion().getModuleName()).endObject();
					builder.field("tagline", "You Know, for Search");
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
					logger.warn("Failed to send response", e);
				}
			}
		});
	}
}
