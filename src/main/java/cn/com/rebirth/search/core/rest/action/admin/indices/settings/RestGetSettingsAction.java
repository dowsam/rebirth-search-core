/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestGetSettingsAction.java 2012-7-6 14:29:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.settings;

import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.splitIndices;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.settings.SettingsFilter;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateRequest;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestGetSettingsAction.
 *
 * @author l.xue.nong
 */
public class RestGetSettingsAction extends BaseRestHandler {

	/** The settings filter. */
	private final SettingsFilter settingsFilter;

	/**
	 * Instantiates a new rest get settings action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 * @param settingsFilter the settings filter
	 */
	@Inject
	public RestGetSettingsAction(Settings settings, Client client, RestController controller,
			SettingsFilter settingsFilter) {
		super(settings, client);
		controller.registerHandler(GET, "/_settings", this);
		controller.registerHandler(GET, "/{index}/_settings", this);

		this.settingsFilter = settingsFilter;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		final String[] indices = splitIndices(request.param("index"));

		ClusterStateRequest clusterStateRequest = Requests.clusterStateRequest().filterRoutingTable(true)
				.filterNodes(true).filteredIndices(indices);

		client.admin().cluster().state(clusterStateRequest, new ActionListener<ClusterStateResponse>() {
			@Override
			public void onResponse(ClusterStateResponse response) {
				try {
					MetaData metaData = response.state().metaData();
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();

					for (IndexMetaData indexMetaData : metaData) {
						builder.startObject(indexMetaData.index(), XContentBuilder.FieldCaseConversion.NONE);

						builder.startObject("settings");
						Settings settings = settingsFilter.filterSettings(indexMetaData.settings());
						for (Map.Entry<String, String> entry : settings.getAsMap().entrySet()) {
							builder.field(entry.getKey(), entry.getValue());
						}
						builder.endObject();

						builder.endObject();
					}

					builder.endObject();

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
