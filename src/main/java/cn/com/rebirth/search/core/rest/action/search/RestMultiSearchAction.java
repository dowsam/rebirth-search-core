/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestMultiSearchAction.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.search;

import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestStatus.BAD_REQUEST;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder.restContentBuilder;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.search.MultiSearchRequest;
import cn.com.rebirth.search.core.action.search.MultiSearchResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestActions;

/**
 * The Class RestMultiSearchAction.
 *
 * @author l.xue.nong
 */
public class RestMultiSearchAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest multi search action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestMultiSearchAction(Settings settings, Client client, RestController controller) {
		super(settings, client);

		controller.registerHandler(GET, "/_msearch", this);
		controller.registerHandler(POST, "/_msearch", this);
		controller.registerHandler(GET, "/{index}/_msearch", this);
		controller.registerHandler(POST, "/{index}/_msearch", this);
		controller.registerHandler(GET, "/{index}/{type}/_msearch", this);
		controller.registerHandler(POST, "/{index}/{type}/_msearch", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

		String[] indices = RestActions.splitIndices(request.param("index"));
		String[] types = RestActions.splitTypes(request.param("type"));

		try {
			multiSearchRequest.add(request.contentByteArray(), request.contentByteArrayOffset(),
					request.contentLength(), request.contentUnsafe(), indices, types);
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

		client.multiSearch(multiSearchRequest, new ActionListener<MultiSearchResponse>() {
			@Override
			public void onResponse(MultiSearchResponse response) {
				try {
					XContentBuilder builder = restContentBuilder(request);
					builder.startObject();
					response.toXContent(builder, request);
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
