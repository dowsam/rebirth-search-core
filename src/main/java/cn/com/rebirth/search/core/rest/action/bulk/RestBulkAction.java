/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestBulkAction.java 2012-3-29 15:02:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest.action.bulk;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.bulk.BulkItemResponse;
import cn.com.rebirth.search.core.action.bulk.BulkRequest;
import cn.com.rebirth.search.core.action.bulk.BulkResponse;
import cn.com.rebirth.search.core.action.index.IndexResponse;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.PUT;
import static cn.com.rebirth.search.core.rest.RestStatus.BAD_REQUEST;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder.restContentBuilder;


/**
 * The Class RestBulkAction.
 *
 * @author l.xue.nong
 */
public class RestBulkAction extends BaseRestHandler {

	
	/**
	 * Instantiates a new rest bulk action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestBulkAction(Settings settings, Client client, RestController controller) {
		super(settings, client);

		controller.registerHandler(POST, "/_bulk", this);
		controller.registerHandler(PUT, "/_bulk", this);
		controller.registerHandler(POST, "/{index}/_bulk", this);
		controller.registerHandler(PUT, "/{index}/_bulk", this);
		controller.registerHandler(POST, "/{index}/{type}/_bulk", this);
		controller.registerHandler(PUT, "/{index}/{type}/_bulk", this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		BulkRequest bulkRequest = Requests.bulkRequest();
		String defaultIndex = request.param("index");
		String defaultType = request.param("type");

		String replicationType = request.param("replication");
		if (replicationType != null) {
			bulkRequest.replicationType(ReplicationType.fromString(replicationType));
		}
		String consistencyLevel = request.param("consistency");
		if (consistencyLevel != null) {
			bulkRequest.consistencyLevel(WriteConsistencyLevel.fromString(consistencyLevel));
		}
		bulkRequest.refresh(request.paramAsBoolean("refresh", bulkRequest.refresh()));
		try {
			bulkRequest.add(request.contentByteArray(), request.contentByteArrayOffset(), request.contentLength(),
					request.contentUnsafe(), defaultIndex, defaultType);
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

		client.bulk(bulkRequest, new ActionListener<BulkResponse>() {
			@Override
			public void onResponse(BulkResponse response) {
				try {
					XContentBuilder builder = restContentBuilder(request);
					builder.startObject();
					builder.field(Fields.TOOK, response.tookInMillis());
					builder.startArray(Fields.ITEMS);
					for (BulkItemResponse itemResponse : response) {
						builder.startObject();
						builder.startObject(itemResponse.opType());
						builder.field(Fields._INDEX, itemResponse.index());
						builder.field(Fields._TYPE, itemResponse.type());
						builder.field(Fields._ID, itemResponse.id());
						long version = itemResponse.version();
						if (version != -1) {
							builder.field(Fields._VERSION, itemResponse.version());
						}
						if (itemResponse.failed()) {
							builder.field(Fields.ERROR, itemResponse.failure().message());
						} else {
							builder.field(Fields.OK, true);
						}
						if (itemResponse.response() instanceof IndexResponse) {
							IndexResponse indexResponse = itemResponse.response();
							if (indexResponse.matches() != null) {
								builder.startArray(Fields.MATCHES);
								for (String match : indexResponse.matches()) {
									builder.value(match);
								}
								builder.endArray();
							}
						}
						builder.endObject();
						builder.endObject();
					}
					builder.endArray();

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

	
	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		
		/** The Constant ITEMS. */
		static final XContentBuilderString ITEMS = new XContentBuilderString("items");

		
		/** The Constant _INDEX. */
		static final XContentBuilderString _INDEX = new XContentBuilderString("_index");

		
		/** The Constant _TYPE. */
		static final XContentBuilderString _TYPE = new XContentBuilderString("_type");

		
		/** The Constant _ID. */
		static final XContentBuilderString _ID = new XContentBuilderString("_id");

		
		/** The Constant ERROR. */
		static final XContentBuilderString ERROR = new XContentBuilderString("error");

		
		/** The Constant OK. */
		static final XContentBuilderString OK = new XContentBuilderString("ok");

		
		/** The Constant TOOK. */
		static final XContentBuilderString TOOK = new XContentBuilderString("took");

		
		/** The Constant _VERSION. */
		static final XContentBuilderString _VERSION = new XContentBuilderString("_version");

		
		/** The Constant MATCHES. */
		static final XContentBuilderString MATCHES = new XContentBuilderString("matches");
	}

}
