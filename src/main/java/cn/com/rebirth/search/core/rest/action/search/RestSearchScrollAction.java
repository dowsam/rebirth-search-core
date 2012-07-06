/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestSearchScrollAction.java 2012-3-29 15:02:19 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest.action.search;

import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestStatus.BAD_REQUEST;
import static cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder.restContentBuilder;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.search.SearchOperationThreading;
import cn.com.rebirth.search.core.action.search.SearchResponse;
import cn.com.rebirth.search.core.action.search.SearchScrollRequest;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.search.Scroll;


/**
 * The Class RestSearchScrollAction.
 *
 * @author l.xue.nong
 */
public class RestSearchScrollAction extends BaseRestHandler {

	
	/**
	 * Instantiates a new rest search scroll action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestSearchScrollAction(Settings settings, Client client, RestController controller) {
		super(settings, client);

		controller.registerHandler(GET, "/_search/scroll", this);
		controller.registerHandler(POST, "/_search/scroll", this);
		controller.registerHandler(GET, "/_search/scroll/{scroll_id}", this);
		controller.registerHandler(POST, "/_search/scroll/{scroll_id}", this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		String scrollId = request.param("scroll_id");
		if (scrollId == null && request.hasContent()) {
			scrollId = request.contentAsString();
		}
		SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);
		try {
			String scroll = request.param("scroll");
			if (scroll != null) {
				searchScrollRequest.scroll(new Scroll(TimeValue.parseTimeValue(scroll, null)));
			}
			searchScrollRequest.listenerThreaded(false);
			SearchOperationThreading operationThreading = SearchOperationThreading.fromString(
					request.param("operation_threading"), null);
			if (operationThreading != null) {
				if (operationThreading == SearchOperationThreading.NO_THREADS) {
					
					operationThreading = SearchOperationThreading.SINGLE_THREAD;
				}
				searchScrollRequest.operationThreading(operationThreading);
			}
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

		client.searchScroll(searchScrollRequest, new ActionListener<SearchResponse>() {
			@Override
			public void onResponse(SearchResponse response) {
				try {
					XContentBuilder builder = restContentBuilder(request);
					builder.startObject();
					response.toXContent(builder, request);
					builder.endObject();
					channel.sendResponse(new XContentRestResponse(request, response.status(), builder));
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
