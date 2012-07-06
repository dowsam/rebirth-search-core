/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestAnalyzeAction.java 2012-7-6 14:28:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.analyze;

import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder.restContentBuilder;

import java.io.IOException;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.analyze.AnalyzeRequest;
import cn.com.rebirth.search.core.action.admin.indices.analyze.AnalyzeResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;

/**
 * The Class RestAnalyzeAction.
 *
 * @author l.xue.nong
 */
public class RestAnalyzeAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest analyze action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestAnalyzeAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(GET, "/_analyze", this);
		controller.registerHandler(GET, "/{index}/_analyze", this);
		controller.registerHandler(POST, "/_analyze", this);
		controller.registerHandler(POST, "/{index}/_analyze", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		String text = request.param("text");
		if (text == null && request.hasContent()) {
			text = request.contentAsString();
		}
		if (text == null) {
			try {
				channel.sendResponse(new XContentThrowableRestResponse(request, new RebirthIllegalArgumentException(
						"text is missing")));
			} catch (IOException e1) {
				logger.warn("Failed to send response", e1);
			}
			return;
		}

		AnalyzeRequest analyzeRequest = new AnalyzeRequest(request.param("index"), text);
		analyzeRequest.preferLocal(request.paramAsBoolean("prefer_local", analyzeRequest.preferLocalShard()));
		analyzeRequest.analyzer(request.param("analyzer"));
		analyzeRequest.field(request.param("field"));
		analyzeRequest.tokenizer(request.param("tokenizer"));
		analyzeRequest.tokenFilters(request.paramAsStringArray("token_filters",
				request.paramAsStringArray("filters", null)));
		client.admin().indices().analyze(analyzeRequest, new ActionListener<AnalyzeResponse>() {
			@Override
			public void onResponse(AnalyzeResponse response) {
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
