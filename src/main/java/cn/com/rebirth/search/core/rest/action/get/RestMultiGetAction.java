/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestMultiGetAction.java 2012-3-29 15:02:37 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest.action.get;

import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestStatus.BAD_REQUEST;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder.restContentBuilder;

import java.io.IOException;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.get.MultiGetRequest;
import cn.com.rebirth.search.core.action.get.MultiGetResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;


/**
 * The Class RestMultiGetAction.
 *
 * @author l.xue.nong
 */
public class RestMultiGetAction extends BaseRestHandler {

	
	/**
	 * Instantiates a new rest multi get action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestMultiGetAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(GET, "/_mget", this);
		controller.registerHandler(POST, "/_mget", this);
		controller.registerHandler(GET, "/{index}/_mget", this);
		controller.registerHandler(POST, "/{index}/_mget", this);
		controller.registerHandler(GET, "/{index}/{type}/_mget", this);
		controller.registerHandler(POST, "/{index}/{type}/_mget", this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		MultiGetRequest multiGetRequest = new MultiGetRequest();
		multiGetRequest.listenerThreaded(false);
		multiGetRequest.refresh(request.paramAsBoolean("refresh", multiGetRequest.refresh()));
		multiGetRequest.preference(request.param("preference"));
		multiGetRequest.realtime(request.paramAsBooleanOptional("realtime", null));

		String[] sFields = null;
		String sField = request.param("fields");
		if (sField != null) {
			sFields = Strings.splitStringByCommaToArray(sField);
		}

		try {
			multiGetRequest.add(request.param("index"), request.param("type"), sFields, request.contentByteArray(),
					request.contentByteArrayOffset(), request.contentLength());
		} catch (Exception e) {
			try {
				XContentBuilder builder = restContentBuilder(request);
				channel.sendResponse(new XContentRestResponse(request, BAD_REQUEST, builder.startObject()
						.field("error", e.getMessage()).endObject()));
			} catch (IOException e1) {
				logger.error("Failed to send failure response", e1);
			}
			return;
		}

		client.multiGet(multiGetRequest, new ActionListener<MultiGetResponse>() {
			@Override
			public void onResponse(MultiGetResponse response) {
				try {
					XContentBuilder builder = restContentBuilder(request);
					response.toXContent(builder, request);
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