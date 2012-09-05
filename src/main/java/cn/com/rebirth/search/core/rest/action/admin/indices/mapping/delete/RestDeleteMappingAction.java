/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestDeleteMappingAction.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.mapping.delete;

import static cn.com.rebirth.search.core.client.Requests.deleteMappingRequest;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.DELETE;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.splitIndices;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingRequest;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestDeleteMappingAction.
 *
 * @author l.xue.nong
 */
public class RestDeleteMappingAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest delete mapping action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestDeleteMappingAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(DELETE, "/{index}/{type}/_mapping", this);
		controller.registerHandler(DELETE, "/{index}/{type}", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		DeleteMappingRequest deleteMappingRequest = deleteMappingRequest(splitIndices(request.param("index")));
		deleteMappingRequest.type(request.param("type"));
		client.admin().indices().deleteMapping(deleteMappingRequest, new ActionListener<DeleteMappingResponse>() {
			@Override
			public void onResponse(DeleteMappingResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject().field("ok", true);
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