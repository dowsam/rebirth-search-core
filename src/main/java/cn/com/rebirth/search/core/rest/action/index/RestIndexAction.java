/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestIndexAction.java 2012-7-6 14:28:52 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.index;

import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.PUT;
import static cn.com.rebirth.search.core.rest.RestStatus.BAD_REQUEST;
import static cn.com.rebirth.search.core.rest.RestStatus.CREATED;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.index.IndexRequest;
import cn.com.rebirth.search.core.action.index.IndexResponse;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.index.VersionType;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestHandler;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestActions;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestIndexAction.
 *
 * @author l.xue.nong
 */
public class RestIndexAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest index action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestIndexAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(POST, "/{index}/{type}", this);
		controller.registerHandler(PUT, "/{index}/{type}/{id}", this);
		controller.registerHandler(POST, "/{index}/{type}/{id}", this);
		controller.registerHandler(PUT, "/{index}/{type}/{id}/_create", new CreateHandler());
		controller.registerHandler(POST, "/{index}/{type}/{id}/_create", new CreateHandler());
	}

	/**
	 * The Class CreateHandler.
	 *
	 * @author l.xue.nong
	 */
	final class CreateHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(RestRequest request, RestChannel channel) {
			request.params().put("op_type", "create");
			RestIndexAction.this.handleRequest(request, channel);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		IndexRequest indexRequest = new IndexRequest(request.param("index"), request.param("type"), request.param("id"));
		indexRequest.routing(request.param("routing"));
		indexRequest.parent(request.param("parent"));
		indexRequest.timestamp(request.param("timestamp"));
		if (request.hasParam("ttl")) {
			indexRequest.ttl(request.paramAsTime("ttl", null).millis());
		}
		indexRequest.source(request.contentByteArray(), request.contentByteArrayOffset(), request.contentLength(),
				request.contentUnsafe());
		indexRequest.timeout(request.paramAsTime("timeout", IndexRequest.DEFAULT_TIMEOUT));
		indexRequest.refresh(request.paramAsBoolean("refresh", indexRequest.refresh()));
		indexRequest.version(RestActions.parseVersion(request));
		indexRequest.versionType(VersionType.fromString(request.param("version_type"), indexRequest.versionType()));
		indexRequest.percolate(request.param("percolate", null));
		String sOpType = request.param("op_type");
		if (sOpType != null) {
			if ("index".equals(sOpType)) {
				indexRequest.opType(IndexRequest.OpType.INDEX);
			} else if ("create".equals(sOpType)) {
				indexRequest.opType(IndexRequest.OpType.CREATE);
			} else {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					channel.sendResponse(new XContentRestResponse(request, BAD_REQUEST, builder
							.startObject()
							.field("error",
									"opType [" + sOpType + "] not allowed, either [index] or [create] are allowed")
							.endObject()));
				} catch (IOException e1) {
					logger.warn("Failed to send response", e1);
					return;
				}
			}
		}
		String replicationType = request.param("replication");
		if (replicationType != null) {
			indexRequest.replicationType(ReplicationType.fromString(replicationType));
		}
		String consistencyLevel = request.param("consistency");
		if (consistencyLevel != null) {
			indexRequest.consistencyLevel(WriteConsistencyLevel.fromString(consistencyLevel));
		}

		indexRequest.listenerThreaded(false);

		indexRequest.operationThreaded(true);
		client.index(indexRequest, new ActionListener<IndexResponse>() {
			@Override
			public void onResponse(IndexResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject().field(Fields.OK, true).field(Fields._INDEX, response.index())
							.field(Fields._TYPE, response.type()).field(Fields._ID, response.id())
							.field(Fields._VERSION, response.version());
					if (response.matches() != null) {
						builder.startArray(Fields.MATCHES);
						for (String match : response.matches()) {
							builder.value(match);
						}
						builder.endArray();
					}
					builder.endObject();
					RestStatus status = OK;
					if (response.version() == 1) {
						status = CREATED;
					}
					channel.sendResponse(new XContentRestResponse(request, status, builder));
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

		/** The Constant _INDEX. */
		static final XContentBuilderString _INDEX = new XContentBuilderString("_index");

		/** The Constant _TYPE. */
		static final XContentBuilderString _TYPE = new XContentBuilderString("_type");

		/** The Constant _ID. */
		static final XContentBuilderString _ID = new XContentBuilderString("_id");

		/** The Constant _VERSION. */
		static final XContentBuilderString _VERSION = new XContentBuilderString("_version");

		/** The Constant MATCHES. */
		static final XContentBuilderString MATCHES = new XContentBuilderString("matches");
	}

}