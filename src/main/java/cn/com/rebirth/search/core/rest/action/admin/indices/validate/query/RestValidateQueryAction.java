/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestValidateQueryAction.java 2012-3-29 15:01:20 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest.action.admin.indices.validate.query;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.BytesStream;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.validate.query.ValidateQueryRequest;
import cn.com.rebirth.search.core.action.admin.indices.validate.query.ValidateQueryResponse;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.RestRequest.Method;
import cn.com.rebirth.search.core.rest.action.support.RestActions;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;


/**
 * The Class RestValidateQueryAction.
 *
 * @author l.xue.nong
 */
public class RestValidateQueryAction extends BaseRestHandler {

	
	/**
	 * Instantiates a new rest validate query action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestValidateQueryAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(Method.GET, "/_validate/query", this);
		controller.registerHandler(Method.POST, "/_validate/query", this);
		controller.registerHandler(Method.GET, "/{index}/_validate/query", this);
		controller.registerHandler(Method.POST, "/{index}/_validate/query", this);
		controller.registerHandler(Method.GET, "/{index}/{type}/_validate/query", this);
		controller.registerHandler(Method.POST, "/{index}/{type}/_validate/query", this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		ValidateQueryRequest validateQueryRequest = new ValidateQueryRequest(RestActions.splitIndices(request
				.param("index")));
		
		validateQueryRequest.listenerThreaded(false);
		try {
			BroadcastOperationThreading operationThreading = BroadcastOperationThreading.fromString(
					request.param("operation_threading"), BroadcastOperationThreading.SINGLE_THREAD);
			if (operationThreading == BroadcastOperationThreading.NO_THREADS) {
				
				operationThreading = BroadcastOperationThreading.SINGLE_THREAD;
			}
			validateQueryRequest.operationThreading(operationThreading);
			if (request.hasContent()) {
				validateQueryRequest.query(request.contentByteArray(), request.contentByteArrayOffset(),
						request.contentLength(), true);
			} else {
				String source = request.param("source");
				if (source != null) {
					validateQueryRequest.query(source);
				} else {
					BytesStream querySource = RestActions.parseQuerySource(request);
					if (querySource != null) {
						validateQueryRequest.query(querySource.underlyingBytes(), 0, querySource.size(), false);
					}
				}
			}
			validateQueryRequest.types(RestActions.splitTypes(request.param("type")));
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

		client.admin().indices().validateQuery(validateQueryRequest, new ActionListener<ValidateQueryResponse>() {
			@Override
			public void onResponse(ValidateQueryResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();
					builder.field("valid", response.valid());

					RestActions.buildBroadcastShardsHeader(builder, response);

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
