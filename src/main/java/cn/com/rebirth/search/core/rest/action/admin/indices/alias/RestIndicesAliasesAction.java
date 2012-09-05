/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestIndicesAliasesAction.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.alias;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.alias.IndicesAliasesRequest;
import cn.com.rebirth.search.core.action.admin.indices.alias.IndicesAliasesResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.cluster.metadata.AliasAction;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestIndicesAliasesAction.
 *
 * @author l.xue.nong
 */
public class RestIndicesAliasesAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest indices aliases action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestIndicesAliasesAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(RestRequest.Method.POST, "/_aliases", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
		try {
			indicesAliasesRequest.timeout(request.paramAsTime("timeout", TimeValue.timeValueSeconds(10)));
			XContentParser parser = XContentFactory.xContent(request.contentByteArray(),
					request.contentByteArrayOffset(), request.contentLength()).createParser(request.contentByteArray(),
					request.contentByteArrayOffset(), request.contentLength());
			XContentParser.Token token = parser.nextToken();
			if (token == null) {
				throw new RebirthIllegalArgumentException("No action is specified");
			}
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.START_ARRAY) {
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						if (token == XContentParser.Token.FIELD_NAME) {
							String action = parser.currentName();
							AliasAction.Type type;
							if ("add".equals(action)) {
								type = AliasAction.Type.ADD;
							} else if ("remove".equals(action)) {
								type = AliasAction.Type.REMOVE;
							} else {
								throw new RebirthIllegalArgumentException("Alias action [" + action + "] not supported");
							}
							String index = null;
							String alias = null;
							Map<String, Object> filter = null;
							String routing = null;
							boolean routingSet = false;
							String indexRouting = null;
							boolean indexRoutingSet = false;
							String searchRouting = null;
							boolean searchRoutingSet = false;
							String currentFieldName = null;
							while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
								if (token == XContentParser.Token.FIELD_NAME) {
									currentFieldName = parser.currentName();
								} else if (token == XContentParser.Token.VALUE_STRING) {
									if ("index".equals(currentFieldName)) {
										index = parser.text();
									} else if ("alias".equals(currentFieldName)) {
										alias = parser.text();
									} else if ("routing".equals(currentFieldName)) {
										routing = parser.textOrNull();
										routingSet = true;
									} else if ("indexRouting".equals(currentFieldName)
											|| "index-routing".equals(currentFieldName)
											|| "index_routing".equals(currentFieldName)) {
										indexRouting = parser.textOrNull();
										indexRoutingSet = true;
									} else if ("searchRouting".equals(currentFieldName)
											|| "search-routing".equals(currentFieldName)
											|| "search_routing".equals(currentFieldName)) {
										searchRouting = parser.textOrNull();
										searchRoutingSet = true;
									}
								} else if (token == XContentParser.Token.START_OBJECT) {
									if ("filter".equals(currentFieldName)) {
										filter = parser.mapOrdered();
									}
								}
							}
							if (index == null) {
								throw new RebirthIllegalArgumentException("Alias action [" + action
										+ "] requires an [index] to be set");
							}
							if (alias == null) {
								throw new RebirthIllegalArgumentException("Alias action [" + action
										+ "] requires an [alias] to be set");
							}
							if (type == AliasAction.Type.ADD) {
								AliasAction aliasAction = AliasAction.newAddAliasAction(index, alias).filter(filter);
								if (routingSet) {
									aliasAction.routing(routing);
								}
								if (indexRoutingSet) {
									aliasAction.indexRouting(indexRouting);
								}
								if (searchRoutingSet) {
									aliasAction.searchRouting(searchRouting);
								}
								indicesAliasesRequest.addAliasAction(aliasAction);
							} else if (type == AliasAction.Type.REMOVE) {
								indicesAliasesRequest.removeAlias(index, alias);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			try {
				channel.sendResponse(new XContentThrowableRestResponse(request, e));
			} catch (IOException e1) {
				logger.warn("Failed to send response", e1);
			}
			return;
		}
		client.admin().indices().aliases(indicesAliasesRequest, new ActionListener<IndicesAliasesResponse>() {
			@Override
			public void onResponse(IndicesAliasesResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject().field("ok", true).field("acknowledged", response.acknowledged()).endObject();
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