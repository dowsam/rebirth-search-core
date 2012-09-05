/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestClusterGetSettingsAction.java 2012-7-6 14:29:03 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.cluster.settings;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateRequest;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestClusterGetSettingsAction.
 *
 * @author l.xue.nong
 */
public class RestClusterGetSettingsAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest cluster get settings action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestClusterGetSettingsAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(RestRequest.Method.GET, "/_cluster/settings", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		ClusterStateRequest clusterStateRequest = Requests.clusterStateRequest().filterRoutingTable(true)
				.filterNodes(true);
		client.admin().cluster().state(clusterStateRequest, new ActionListener<ClusterStateResponse>() {
			@Override
			public void onResponse(ClusterStateResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();

					builder.startObject("persistent");
					for (Map.Entry<String, String> entry : response.state().metaData().persistentSettings().getAsMap()
							.entrySet()) {
						builder.field(entry.getKey(), entry.getValue());
					}
					builder.endObject();

					builder.startObject("transient");
					for (Map.Entry<String, String> entry : response.state().metaData().transientSettings().getAsMap()
							.entrySet()) {
						builder.field(entry.getKey(), entry.getValue());
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
