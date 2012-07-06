/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestGetIndexTemplateAction.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.template.get;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.compress.CompressedString;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.settings.SettingsFilter;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateRequest;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.cluster.metadata.IndexTemplateMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.RestRequest.Method;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestGetIndexTemplateAction.
 *
 * @author l.xue.nong
 */
public class RestGetIndexTemplateAction extends BaseRestHandler {

	/** The settings filter. */
	private final SettingsFilter settingsFilter;

	/**
	 * Instantiates a new rest get index template action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 * @param settingsFilter the settings filter
	 */
	@Inject
	public RestGetIndexTemplateAction(Settings settings, Client client, RestController controller,
			SettingsFilter settingsFilter) {
		super(settings, client);
		this.settingsFilter = settingsFilter;

		controller.registerHandler(Method.GET, "/_template/{name}", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		ClusterStateRequest clusterStateRequest = Requests.clusterStateRequest().filterRoutingTable(true)
				.filterNodes(true).filteredIndexTemplates(request.param("name")).filteredIndices("_na");

		client.admin().cluster().state(clusterStateRequest, new ActionListener<ClusterStateResponse>() {
			@Override
			public void onResponse(ClusterStateResponse response) {
				try {
					MetaData metaData = response.state().metaData();
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();

					for (IndexTemplateMetaData indexMetaData : metaData.templates().values()) {
						builder.startObject(indexMetaData.name(), XContentBuilder.FieldCaseConversion.NONE);

						builder.field("template", indexMetaData.template());
						builder.field("order", indexMetaData.order());

						builder.startObject("settings");
						Settings settings = settingsFilter.filterSettings(indexMetaData.settings());
						for (Map.Entry<String, String> entry : settings.getAsMap().entrySet()) {
							builder.field(entry.getKey(), entry.getValue());
						}
						builder.endObject();

						builder.startObject("mappings");
						for (Map.Entry<String, CompressedString> entry : indexMetaData.mappings().entrySet()) {
							byte[] mappingSource = entry.getValue().uncompressed();
							XContentParser parser = XContentFactory.xContent(mappingSource).createParser(mappingSource);
							Map<String, Object> mapping = parser.map();
							if (mapping.size() == 1 && mapping.containsKey(entry.getKey())) {

								mapping = (Map<String, Object>) mapping.get(entry.getKey());
							}
							builder.field(entry.getKey());
							builder.map(mapping);
						}
						builder.endObject();

						builder.endObject();
					}

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
