/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestUpdateSettingsAction.java 2012-7-6 14:28:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.settings;

import static cn.com.rebirth.search.core.client.Requests.updateSettingsRequest;
import static cn.com.rebirth.search.core.rest.RestStatus.BAD_REQUEST;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.splitIndices;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.settings.SettingsException;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.settings.UpdateSettingsRequest;
import cn.com.rebirth.search.core.action.admin.indices.settings.UpdateSettingsResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestUpdateSettingsAction.
 *
 * @author l.xue.nong
 */
public class RestUpdateSettingsAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest update settings action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestUpdateSettingsAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(RestRequest.Method.PUT, "/{index}/_settings", this);
		controller.registerHandler(RestRequest.Method.PUT, "/_settings", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		UpdateSettingsRequest updateSettingsRequest = updateSettingsRequest(splitIndices(request.param("index")));
		ImmutableSettings.Builder updateSettings = ImmutableSettings.settingsBuilder();
		String bodySettings = request.contentAsString();
		if (Strings.hasText(bodySettings)) {
			try {
				updateSettings.put(ImmutableSettings.settingsBuilder().loadFromSource(bodySettings).build());
			} catch (Exception e) {
				try {
					channel.sendResponse(new XContentThrowableRestResponse(request, BAD_REQUEST, new SettingsException(
							"Failed to parse index settings", e)));
				} catch (IOException e1) {
					logger.warn("Failed to send response", e1);
				}
				return;
			}
		}
		for (Map.Entry<String, String> entry : request.params().entrySet()) {
			if (entry.getKey().equals("pretty")) {
				continue;
			}
			updateSettings.put(entry.getKey(), entry.getValue());
		}
		updateSettingsRequest.settings(updateSettings);

		client.admin().indices().updateSettings(updateSettingsRequest, new ActionListener<UpdateSettingsResponse>() {
			@Override
			public void onResponse(UpdateSettingsResponse updateSettingsResponse) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject().field("ok", true).endObject();
					channel.sendResponse(new XContentRestResponse(request, OK, builder));
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
