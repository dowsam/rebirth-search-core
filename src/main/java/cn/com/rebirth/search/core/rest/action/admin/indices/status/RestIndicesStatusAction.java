/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestIndicesStatusAction.java 2012-3-29 15:00:50 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest.action.admin.indices.status;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.settings.SettingsFilter;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.status.IndicesStatusRequest;
import cn.com.rebirth.search.core.action.admin.indices.status.IndicesStatusResponse;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.buildBroadcastShardsHeader;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.splitIndices;


/**
 * The Class RestIndicesStatusAction.
 *
 * @author l.xue.nong
 */
public class RestIndicesStatusAction extends BaseRestHandler {

	
	/** The settings filter. */
	private final SettingsFilter settingsFilter;

	
	/**
	 * Instantiates a new rest indices status action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 * @param settingsFilter the settings filter
	 */
	@Inject
	public RestIndicesStatusAction(Settings settings, Client client, RestController controller,
			SettingsFilter settingsFilter) {
		super(settings, client);
		controller.registerHandler(GET, "/_status", this);
		controller.registerHandler(GET, "/{index}/_status", this);

		this.settingsFilter = settingsFilter;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		IndicesStatusRequest indicesStatusRequest = new IndicesStatusRequest(splitIndices(request.param("index")));
		
		indicesStatusRequest.listenerThreaded(false);
		indicesStatusRequest.recovery(request.paramAsBoolean("recovery", indicesStatusRequest.recovery()));
		indicesStatusRequest.snapshot(request.paramAsBoolean("snapshot", indicesStatusRequest.snapshot()));
		BroadcastOperationThreading operationThreading = BroadcastOperationThreading.fromString(
				request.param("operation_threading"), BroadcastOperationThreading.SINGLE_THREAD);
		if (operationThreading == BroadcastOperationThreading.NO_THREADS) {
			
			operationThreading = BroadcastOperationThreading.SINGLE_THREAD;
		}
		indicesStatusRequest.operationThreading(operationThreading);
		client.admin().indices().status(indicesStatusRequest, new ActionListener<IndicesStatusResponse>() {
			@Override
			public void onResponse(IndicesStatusResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();
					builder.field("ok", true);
					buildBroadcastShardsHeader(builder, response);
					response.toXContent(builder, request, settingsFilter);
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