/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestClusterRerouteAction.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.cluster.reroute;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.settings.SettingsFilter;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.reroute.ClusterRerouteRequest;
import cn.com.rebirth.search.core.action.admin.cluster.reroute.ClusterRerouteResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.StringRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;

/**
 * The Class RestClusterRerouteAction.
 *
 * @author l.xue.nong
 */
public class RestClusterRerouteAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest cluster reroute action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 * @param settingsFilter the settings filter
	 */
	@Inject
	public RestClusterRerouteAction(Settings settings, Client client, RestController controller,
			SettingsFilter settingsFilter) {
		super(settings, client);
		controller.registerHandler(RestRequest.Method.POST, "/_cluster/reroute", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		final ClusterRerouteRequest clusterRerouteRequest = Requests.clusterRerouteRequest();
		client.admin().cluster().reroute(clusterRerouteRequest, new ActionListener<ClusterRerouteResponse>() {
			@Override
			public void onResponse(ClusterRerouteResponse response) {
				try {
					channel.sendResponse(new StringRestResponse(RestStatus.OK));
				} catch (Exception e) {
					onFailure(e);
				}
			}

			@Override
			public void onFailure(Throwable e) {
				if (logger.isDebugEnabled()) {
					logger.debug("failed to handle cluster reroute", e);
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