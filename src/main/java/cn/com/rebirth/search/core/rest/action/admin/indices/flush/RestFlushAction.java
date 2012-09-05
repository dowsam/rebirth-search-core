/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestFlushAction.java 2012-7-6 14:30:24 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.flush;

import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.buildBroadcastShardsHeader;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.flush.FlushRequest;
import cn.com.rebirth.search.core.action.admin.indices.flush.FlushResponse;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestActions;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestFlushAction.
 *
 * @author l.xue.nong
 */
public class RestFlushAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest flush action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestFlushAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(POST, "/_flush", this);
		controller.registerHandler(POST, "/{index}/_flush", this);

		controller.registerHandler(GET, "/_flush", this);
		controller.registerHandler(GET, "/{index}/_flush", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		FlushRequest flushRequest = new FlushRequest(RestActions.splitIndices(request.param("index")));

		flushRequest.listenerThreaded(false);
		BroadcastOperationThreading operationThreading = BroadcastOperationThreading.fromString(
				request.param("operationThreading"), BroadcastOperationThreading.SINGLE_THREAD);
		if (operationThreading == BroadcastOperationThreading.NO_THREADS) {

			operationThreading = BroadcastOperationThreading.THREAD_PER_SHARD;
		}
		flushRequest.operationThreading(operationThreading);
		flushRequest.refresh(request.paramAsBoolean("refresh", flushRequest.refresh()));
		flushRequest.full(request.paramAsBoolean("full", flushRequest.full()));
		flushRequest.force(request.paramAsBoolean("force", flushRequest.force()));
		client.admin().indices().flush(flushRequest, new ActionListener<FlushResponse>() {
			@Override
			public void onResponse(FlushResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();
					builder.field("ok", true);

					buildBroadcastShardsHeader(builder, response);

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