/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestGetAction.java 2012-3-29 15:01:10 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest.action.get;

import java.io.IOException;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.get.GetRequest;
import cn.com.rebirth.search.core.action.get.GetResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.RestRequest.Method;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;


/**
 * The Class RestGetAction.
 *
 * @author l.xue.nong
 */
public class RestGetAction extends BaseRestHandler {

	
	/**
	 * Instantiates a new rest get action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestGetAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(Method.GET, "/{index}/{type}/{id}", this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		final GetRequest getRequest = new GetRequest(request.param("index"), request.param("type"), request.param("id"));
		
		getRequest.listenerThreaded(false);
		
		getRequest.operationThreaded(true);
		getRequest.refresh(request.paramAsBoolean("refresh", getRequest.refresh()));
		getRequest.routing(request.param("routing"));
		getRequest.preference(request.param("preference"));
		getRequest.realtime(request.paramAsBooleanOptional("realtime", null));

		String sField = request.param("fields");
		if (sField != null) {
			String[] sFields = Strings.splitStringByCommaToArray(sField);
			if (sFields != null) {
				getRequest.fields(sFields);
			}
		}

		client.get(getRequest, new ActionListener<GetResponse>() {
			@Override
			public void onResponse(GetResponse response) {

				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					response.toXContent(builder, request);
					if (!response.exists()) {
						channel.sendResponse(new XContentRestResponse(request, RestStatus.NOT_FOUND, builder));
					} else {
						channel.sendResponse(new XContentRestResponse(request, RestStatus.OK, builder));
					}
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