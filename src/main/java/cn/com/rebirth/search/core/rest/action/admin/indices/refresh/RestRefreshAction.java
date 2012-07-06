/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestRefreshAction.java 2012-3-29 15:01:09 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest.action.admin.indices.refresh;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.refresh.RefreshRequest;
import cn.com.rebirth.search.core.action.admin.indices.refresh.RefreshResponse;
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
import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.buildBroadcastShardsHeader;


/**
 * The Class RestRefreshAction.
 *
 * @author l.xue.nong
 */
public class RestRefreshAction extends BaseRestHandler {

	
	/**
	 * Instantiates a new rest refresh action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestRefreshAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(POST, "/_refresh", this);
		controller.registerHandler(POST, "/{index}/_refresh", this);

		controller.registerHandler(GET, "/_refresh", this);
		controller.registerHandler(GET, "/{index}/_refresh", this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		RefreshRequest refreshRequest = new RefreshRequest(RestActions.splitIndices(request.param("index")));
		
		refreshRequest.listenerThreaded(false);
		BroadcastOperationThreading operationThreading = BroadcastOperationThreading.fromString(
				request.param("operation_threading"), BroadcastOperationThreading.SINGLE_THREAD);
		if (operationThreading == BroadcastOperationThreading.NO_THREADS) {
			
			operationThreading = BroadcastOperationThreading.THREAD_PER_SHARD;
		}
		refreshRequest.operationThreading(operationThreading);
		client.admin().indices().refresh(refreshRequest, new ActionListener<RefreshResponse>() {
			@Override
			public void onResponse(RefreshResponse response) {
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