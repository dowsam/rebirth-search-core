/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestPercolateAction.java 2012-7-6 14:29:07 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.percolate;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.percolate.PercolateRequest;
import cn.com.rebirth.search.core.action.percolate.PercolateResponse;
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
 * The Class RestPercolateAction.
 *
 * @author l.xue.nong
 */
public class RestPercolateAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest percolate action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestPercolateAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(Method.GET, "/{index}/{type}/_percolate", this);
		controller.registerHandler(Method.POST, "/{index}/{type}/_percolate", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		PercolateRequest percolateRequest = new PercolateRequest(request.param("index"), request.param("type"));
		percolateRequest.source(request.contentByteArray(), request.contentByteArrayOffset(), request.contentLength(),
				request.contentUnsafe());

		percolateRequest.listenerThreaded(false);

		percolateRequest.operationThreaded(true);

		percolateRequest.preferLocal(request.paramAsBoolean("prefer_local", percolateRequest.preferLocalShard()));
		client.percolate(percolateRequest, new ActionListener<PercolateResponse>() {
			@Override
			public void onResponse(PercolateResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();

					builder.field(Fields.OK, true);
					builder.startArray(Fields.MATCHES);
					for (String match : response) {
						builder.value(match);
					}
					builder.endArray();

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

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant OK. */
		static final XContentBuilderString OK = new XContentBuilderString("ok");

		/** The Constant MATCHES. */
		static final XContentBuilderString MATCHES = new XContentBuilderString("matches");
	}
}
