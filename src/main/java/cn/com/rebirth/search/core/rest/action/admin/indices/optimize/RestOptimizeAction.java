/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestOptimizeAction.java 2012-7-6 14:30:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.optimize;

import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestStatus.BAD_REQUEST;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.buildBroadcastShardsHeader;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.optimize.OptimizeRequest;
import cn.com.rebirth.search.core.action.admin.indices.optimize.OptimizeResponse;
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
 * The Class RestOptimizeAction.
 *
 * @author l.xue.nong
 */
public class RestOptimizeAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest optimize action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestOptimizeAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(POST, "/_optimize", this);
		controller.registerHandler(POST, "/{index}/_optimize", this);

		controller.registerHandler(GET, "/_optimize", this);
		controller.registerHandler(GET, "/{index}/_optimize", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		OptimizeRequest optimizeRequest = new OptimizeRequest(RestActions.splitIndices(request.param("index")));
		try {
			optimizeRequest.waitForMerge(request.paramAsBoolean("wait_for_merge", optimizeRequest.waitForMerge()));
			optimizeRequest.maxNumSegments(request.paramAsInt("max_num_segments", optimizeRequest.maxNumSegments()));
			optimizeRequest.onlyExpungeDeletes(request.paramAsBoolean("only_expunge_deletes",
					optimizeRequest.onlyExpungeDeletes()));
			optimizeRequest.flush(request.paramAsBoolean("flush", optimizeRequest.flush()));
			optimizeRequest.refresh(request.paramAsBoolean("refresh", optimizeRequest.refresh()));

			optimizeRequest.listenerThreaded(false);
			BroadcastOperationThreading operationThreading = BroadcastOperationThreading.fromString(
					request.param("operation_threading"), BroadcastOperationThreading.SINGLE_THREAD);
			if (operationThreading == BroadcastOperationThreading.NO_THREADS) {

				operationThreading = BroadcastOperationThreading.THREAD_PER_SHARD;
			}
			optimizeRequest.operationThreading(operationThreading);
		} catch (Exception e) {
			try {
				XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
				channel.sendResponse(new XContentRestResponse(request, BAD_REQUEST, builder.startObject()
						.field("error", e.getMessage()).endObject()));
			} catch (IOException e1) {
				logger.error("Failed to send failure response", e1);
			}
			return;
		}
		client.admin().indices().optimize(optimizeRequest, new ActionListener<OptimizeResponse>() {
			@Override
			public void onResponse(OptimizeResponse response) {
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