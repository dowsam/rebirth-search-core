/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestGetMappingAction.java 2012-7-6 14:29:15 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.mapping.get;

import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.splitIndices;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.splitTypes;

import java.io.IOException;
import java.util.Set;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateRequest;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MappingMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.indices.IndexMissingException;
import cn.com.rebirth.search.core.indices.TypeMissingException;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

import com.google.common.collect.ImmutableSet;

/**
 * The Class RestGetMappingAction.
 *
 * @author l.xue.nong
 */
public class RestGetMappingAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest get mapping action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestGetMappingAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(GET, "/_mapping", this);
		controller.registerHandler(GET, "/{index}/_mapping", this);
		controller.registerHandler(GET, "/{index}/{type}/_mapping", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		final String[] indices = splitIndices(request.param("index"));
		final Set<String> types = ImmutableSet.copyOf(splitTypes(request.param("type")));

		ClusterStateRequest clusterStateRequest = Requests.clusterStateRequest().filterRoutingTable(true)
				.filterNodes(true).filteredIndices(indices);

		client.admin().cluster().state(clusterStateRequest, new ActionListener<ClusterStateResponse>() {
			@Override
			public void onResponse(ClusterStateResponse response) {
				try {
					MetaData metaData = response.state().metaData();
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();

					if (indices.length == 1 && types.size() == 1) {
						if (metaData.indices().isEmpty()) {
							channel.sendResponse(new XContentThrowableRestResponse(request, new IndexMissingException(
									new Index(indices[0]))));
							return;
						}
						boolean foundType = false;
						IndexMetaData indexMetaData = metaData.iterator().next();
						for (MappingMetaData mappingMd : indexMetaData.mappings().values()) {
							if (!types.isEmpty() && !types.contains(mappingMd.type())) {

								continue;
							}
							foundType = true;
							builder.field(mappingMd.type());
							builder.map(mappingMd.sourceAsMap());
						}
						if (!foundType) {
							channel.sendResponse(new XContentThrowableRestResponse(request, new TypeMissingException(
									new Index(indices[0]), types.iterator().next())));
							return;
						}
					} else {
						for (IndexMetaData indexMetaData : metaData) {
							builder.startObject(indexMetaData.index(), XContentBuilder.FieldCaseConversion.NONE);

							for (MappingMetaData mappingMd : indexMetaData.mappings().values()) {
								if (!types.isEmpty() && !types.contains(mappingMd.type())) {

									continue;
								}
								builder.field(mappingMd.type());
								builder.map(mappingMd.sourceAsMap());
							}

							builder.endObject();
						}
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
