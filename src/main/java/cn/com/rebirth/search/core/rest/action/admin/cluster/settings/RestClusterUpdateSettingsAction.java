/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestClusterUpdateSettingsAction.java 2012-7-6 14:28:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.cluster.settings;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentType;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import cn.com.rebirth.search.core.action.admin.cluster.settings.ClusterUpdateSettingsResponse;
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
 * The Class RestClusterUpdateSettingsAction.
 *
 * @author l.xue.nong
 */
public class RestClusterUpdateSettingsAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest cluster update settings action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestClusterUpdateSettingsAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(RestRequest.Method.PUT, "/_cluster/settings", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		final ClusterUpdateSettingsRequest clusterUpdateSettingsRequest = Requests.clusterUpdateSettingsRequest();

		try {
			XContentType xContentType = XContentFactory.xContentType(request.contentByteArray(),
					request.contentByteArrayOffset(), request.contentLength());
			Map<String, Object> source = XContentFactory
					.xContent(xContentType)
					.createParser(request.contentByteArray(), request.contentByteArrayOffset(), request.contentLength())
					.mapAndClose();

			if (source.containsKey("transient")) {
				clusterUpdateSettingsRequest.transientSettings((Map) source.get("transient"));
			}
			if (source.containsKey("persistent")) {
				clusterUpdateSettingsRequest.persistentSettings((Map) source.get("persistent"));
			}
		} catch (Exception e) {
			try {
				channel.sendResponse(new XContentThrowableRestResponse(request, e));
			} catch (IOException e1) {
				logger.warn("Failed to send response", e1);
			}
			return;
		}

		client.admin().cluster()
				.updateSettings(clusterUpdateSettingsRequest, new ActionListener<ClusterUpdateSettingsResponse>() {
					@Override
					public void onResponse(ClusterUpdateSettingsResponse response) {
						try {
							channel.sendResponse(new StringRestResponse(RestStatus.OK));
						} catch (Exception e) {
							onFailure(e);
						}
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