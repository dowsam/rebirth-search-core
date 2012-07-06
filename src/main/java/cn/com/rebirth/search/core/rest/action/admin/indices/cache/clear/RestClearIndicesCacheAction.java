/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestClearIndicesCacheAction.java 2012-7-6 14:30:15 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.cache.clear;

import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.cache.clear.ClearIndicesCacheRequest;
import cn.com.rebirth.search.core.action.admin.indices.cache.clear.ClearIndicesCacheResponse;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;
import cn.com.rebirth.search.core.client.Client;
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
 * The Class RestClearIndicesCacheAction.
 *
 * @author l.xue.nong
 */
public class RestClearIndicesCacheAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest clear indices cache action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestClearIndicesCacheAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(POST, "/_cache/clear", this);
		controller.registerHandler(POST, "/{index}/_cache/clear", this);

		controller.registerHandler(GET, "/_cache/clear", this);
		controller.registerHandler(GET, "/{index}/_cache/clear", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		ClearIndicesCacheRequest clearIndicesCacheRequest = new ClearIndicesCacheRequest(
				RestActions.splitIndices(request.param("index")));
		try {
			clearIndicesCacheRequest.filterCache(request.paramAsBoolean("filter",
					clearIndicesCacheRequest.filterCache()));
			clearIndicesCacheRequest.fieldDataCache(request.paramAsBoolean("field_data",
					clearIndicesCacheRequest.fieldDataCache()));
			clearIndicesCacheRequest.idCache(request.paramAsBoolean("id", clearIndicesCacheRequest.idCache()));
			clearIndicesCacheRequest.bloomCache(request.paramAsBoolean("bloom", clearIndicesCacheRequest.bloomCache()));
			clearIndicesCacheRequest.fields(request.paramAsStringArray("fields", clearIndicesCacheRequest.fields()));

			clearIndicesCacheRequest.listenerThreaded(false);
			BroadcastOperationThreading operationThreading = BroadcastOperationThreading.fromString(
					request.param("operationThreading"), BroadcastOperationThreading.SINGLE_THREAD);
			if (operationThreading == BroadcastOperationThreading.NO_THREADS) {

				operationThreading = BroadcastOperationThreading.THREAD_PER_SHARD;
			}
			clearIndicesCacheRequest.operationThreading(operationThreading);
		} catch (Exception e) {
			try {
				XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
				channel.sendResponse(new XContentRestResponse(request, RestStatus.BAD_REQUEST, builder.startObject()
						.field("error", e.getMessage()).endObject()));
			} catch (IOException e1) {
				logger.error("Failed to send failure response", e1);
			}
			return;
		}
		client.admin().indices().clearCache(clearIndicesCacheRequest, new ActionListener<ClearIndicesCacheResponse>() {
			@Override
			public void onResponse(ClearIndicesCacheResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();
					builder.field("ok", true);

					RestActions.buildBroadcastShardsHeader(builder, response);

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