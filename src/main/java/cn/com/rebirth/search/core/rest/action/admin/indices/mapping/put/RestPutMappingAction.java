/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestPutMappingAction.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.mapping.put;

import static cn.com.rebirth.search.core.client.Requests.putMappingRequest;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.PUT;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.splitIndices;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.mapping.put.PutMappingRequest;
import cn.com.rebirth.search.core.action.admin.indices.mapping.put.PutMappingResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestPutMappingAction.
 *
 * @author l.xue.nong
 */
public class RestPutMappingAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest put mapping action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestPutMappingAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(PUT, "/{index}/_mapping", this);
		controller.registerHandler(PUT, "/{index}/{type}/_mapping", this);

		controller.registerHandler(POST, "/{index}/_mapping", this);
		controller.registerHandler(POST, "/{index}/{type}/_mapping", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		PutMappingRequest putMappingRequest = putMappingRequest(splitIndices(request.param("index")));
		putMappingRequest.type(request.param("type"));
		putMappingRequest.source(request.contentAsString());
		putMappingRequest.timeout(request.paramAsTime("timeout", TimeValue.timeValueSeconds(10)));
		putMappingRequest.ignoreConflicts(request.paramAsBoolean("ignore_conflicts",
				putMappingRequest.ignoreConflicts()));
		client.admin().indices().putMapping(putMappingRequest, new ActionListener<PutMappingResponse>() {
			@Override
			public void onResponse(PutMappingResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject().field("ok", true).field("acknowledged", response.acknowledged());
					builder.endObject();
					channel.sendResponse(new XContentRestResponse(request, OK, builder));
				} catch (IOException e) {
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