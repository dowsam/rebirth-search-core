/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestUpdateAction.java 2012-7-6 14:29:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.update;

import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestStatus.CREATED;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentType;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.action.update.UpdateRequest;
import cn.com.rebirth.search.core.action.update.UpdateResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestUpdateAction.
 *
 * @author l.xue.nong
 */
public class RestUpdateAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest update action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestUpdateAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(POST, "/{index}/{type}/{id}/_update", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		UpdateRequest updateRequest = new UpdateRequest(request.param("index"), request.param("type"),
				request.param("id"));
		updateRequest.routing(request.param("routing"));
		updateRequest.parent(request.param("parent"));
		updateRequest.timeout(request.paramAsTime("timeout", updateRequest.timeout()));
		updateRequest.refresh(request.paramAsBoolean("refresh", updateRequest.refresh()));
		String replicationType = request.param("replication");
		if (replicationType != null) {
			updateRequest.replicationType(ReplicationType.fromString(replicationType));
		}
		String consistencyLevel = request.param("consistency");
		if (consistencyLevel != null) {
			updateRequest.consistencyLevel(WriteConsistencyLevel.fromString(consistencyLevel));
		}
		updateRequest.percolate(request.param("percolate", null));

		updateRequest.listenerThreaded(false);
		updateRequest.script(request.param("script"));
		updateRequest.scriptLang(request.param("lang"));
		for (Map.Entry<String, String> entry : request.params().entrySet()) {
			if (entry.getKey().startsWith("sp_")) {
				updateRequest.addScriptParam(entry.getKey().substring(3), entry.getValue());
			}
		}
		updateRequest.retryOnConflict(request.paramAsInt("retry_on_conflict", updateRequest.retryOnConflict()));

		if (request.hasContent()) {
			XContentType xContentType = XContentFactory.xContentType(request.contentByteArray(),
					request.contentByteArrayOffset(), request.contentLength());
			if (xContentType != null) {
				try {
					Map<String, Object> content = XContentFactory
							.xContent(xContentType)
							.createParser(request.contentByteArray(), request.contentByteArrayOffset(),
									request.contentLength()).mapAndClose();
					if (content.containsKey("script")) {
						updateRequest.script(content.get("script").toString());
					}
					if (content.containsKey("lang")) {
						updateRequest.scriptLang(content.get("lang").toString());
					}
					if (content.containsKey("params")) {
						updateRequest.scriptParams((Map<String, Object>) content.get("params"));
					}
				} catch (Exception e) {
					try {
						channel.sendResponse(new XContentThrowableRestResponse(request, e));
					} catch (IOException e1) {
						logger.warn("Failed to send response", e1);
					}
					return;
				}
			}
		}

		client.update(updateRequest, new ActionListener<UpdateResponse>() {
			@Override
			public void onResponse(UpdateResponse response) {
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
