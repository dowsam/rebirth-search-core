/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestCountAction.java 2012-7-6 14:30:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.count;

import static cn.com.rebirth.search.core.action.count.CountRequest.DEFAULT_MIN_SCORE;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestStatus.BAD_REQUEST;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.buildBroadcastShardsHeader;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.splitTypes;

import java.io.IOException;

import cn.com.rebirth.commons.io.BytesStream;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.count.CountRequest;
import cn.com.rebirth.search.core.action.count.CountResponse;
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
 * The Class RestCountAction.
 *
 * @author l.xue.nong
 */
public class RestCountAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest count action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestCountAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(POST, "/_count", this);
		controller.registerHandler(GET, "/_count", this);
		controller.registerHandler(POST, "/{index}/_count", this);
		controller.registerHandler(GET, "/{index}/_count", this);
		controller.registerHandler(POST, "/{index}/{type}/_count", this);
		controller.registerHandler(GET, "/{index}/{type}/_count", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		CountRequest countRequest = new CountRequest(RestActions.splitIndices(request.param("index")));

		countRequest.listenerThreaded(false);
		try {
			BroadcastOperationThreading operationThreading = BroadcastOperationThreading.fromString(
					request.param("operation_threading"), BroadcastOperationThreading.SINGLE_THREAD);
			if (operationThreading == BroadcastOperationThreading.NO_THREADS) {

				operationThreading = BroadcastOperationThreading.SINGLE_THREAD;
			}
			countRequest.operationThreading(operationThreading);
			if (request.hasContent()) {
				countRequest.query(request.contentByteArray(), request.contentByteArrayOffset(),
						request.contentLength(), true);
			} else {
				String source = request.param("source");
				if (source != null) {
					countRequest.query(source);
				} else {
					BytesStream querySource = RestActions.parseQuerySource(request);
					if (querySource != null) {
						countRequest.query(querySource.underlyingBytes(), 0, querySource.size(), false);
					}
				}
			}
			countRequest.queryHint(request.param("query_hint"));
			countRequest.routing(request.param("routing"));
			countRequest.minScore(request.paramAsFloat("min_score", DEFAULT_MIN_SCORE));
			countRequest.types(splitTypes(request.param("type")));
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

		client.count(countRequest, new ActionListener<CountResponse>() {
			@Override
			public void onResponse(CountResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();
					builder.field("count", response.count());

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