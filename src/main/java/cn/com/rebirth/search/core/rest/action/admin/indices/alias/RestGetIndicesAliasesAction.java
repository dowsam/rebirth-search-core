/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestGetIndicesAliasesAction.java 2012-7-6 14:29:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.alias;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateRequest;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.cluster.metadata.AliasMetaData;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestActions;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestGetIndicesAliasesAction.
 *
 * @author l.xue.nong
 */
public class RestGetIndicesAliasesAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest get indices aliases action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestGetIndicesAliasesAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(RestRequest.Method.GET, "/_aliases", this);
		controller.registerHandler(RestRequest.Method.GET, "/{index}/_aliases", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		final String[] indices = RestActions.splitIndices(request.param("index"));

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

						builder.startObject("aliases");
						for (AliasMetaData alias : indexMetaData.aliases().values()) {
							AliasMetaData.Builder.toXContent(alias, builder, ToXContent.EMPTY_PARAMS);
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
