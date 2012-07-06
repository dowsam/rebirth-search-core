/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestDeleteIndexAction.java 2012-3-29 15:01:43 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest.action.admin.indices.delete;

import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.splitIndices;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.delete.DeleteIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.delete.DeleteIndexResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;


/**
 * The Class RestDeleteIndexAction.
 *
 * @author l.xue.nong
 */
public class RestDeleteIndexAction extends BaseRestHandler {

	
	/**
	 * Instantiates a new rest delete index action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestDeleteIndexAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(RestRequest.Method.DELETE, "/", this);
		controller.registerHandler(RestRequest.Method.DELETE, "/{index}", this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(splitIndices(request.param("index")));
		deleteIndexRequest.timeout(request.paramAsTime("timeout", TimeValue.timeValueSeconds(10)));
		client.admin().indices().delete(deleteIndexRequest, new ActionListener<DeleteIndexResponse>() {
			@Override
			public void onResponse(DeleteIndexResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject().field(Fields.OK, true).field(Fields.ACKNOWLEDGED, response.acknowledged())
							.endObject();
					channel.sendResponse(new XContentRestResponse(request, OK, builder));
				} catch (IOException e) {
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

		
		/** The Constant ACKNOWLEDGED. */
		static final XContentBuilderString ACKNOWLEDGED = new XContentBuilderString("acknowledged");
	}
}