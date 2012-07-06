/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestIndicesExistsAction.java 2012-7-6 14:29:36 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.exists;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.settings.SettingsFilter;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.exists.IndicesExistsRequest;
import cn.com.rebirth.search.core.action.admin.indices.exists.IndicesExistsResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.StringRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.HEAD;
import static cn.com.rebirth.search.core.rest.RestStatus.*;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.splitIndices;

/**
 * The Class RestIndicesExistsAction.
 *
 * @author l.xue.nong
 */
public class RestIndicesExistsAction extends BaseRestHandler {

	/** The settings filter. */
	private final SettingsFilter settingsFilter;

	/**
	 * Instantiates a new rest indices exists action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 * @param settingsFilter the settings filter
	 */
	@Inject
	public RestIndicesExistsAction(Settings settings, Client client, RestController controller,
			SettingsFilter settingsFilter) {
		super(settings, client);
		controller.registerHandler(HEAD, "/{index}", this);

		this.settingsFilter = settingsFilter;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		IndicesExistsRequest indicesExistsRequest = new IndicesExistsRequest(splitIndices(request.param("index")));

		indicesExistsRequest.listenerThreaded(false);
		client.admin().indices().exists(indicesExistsRequest, new ActionListener<IndicesExistsResponse>() {
			@Override
			public void onResponse(IndicesExistsResponse response) {
				try {
					if (response.exists()) {
						channel.sendResponse(new StringRestResponse(OK));
					} else {
						channel.sendResponse(new StringRestResponse(NOT_FOUND));
					}
				} catch (Exception e) {
					onFailure(e);
				}
			}

			@Override
			public void onFailure(Throwable e) {
				try {
					channel.sendResponse(new XContentThrowableRestResponse(request, e));
				} catch (Exception e1) {
					logger.error("Failed to send failure response", e1);
				}
			}
		});
	}
}
