/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestDeleteAction.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.delete;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.delete.DeleteRequest;
import cn.com.rebirth.search.core.action.delete.DeleteResponse;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.index.VersionType;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.RestRequest.Method;
import cn.com.rebirth.search.core.rest.action.support.RestActions;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class RestDeleteAction.
 *
 * @author l.xue.nong
 */
public class RestDeleteAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest delete action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestDeleteAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(Method.DELETE, "/{index}/{type}/{id}", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		DeleteRequest deleteRequest = new DeleteRequest(request.param("index"), request.param("type"),
				request.param("id"));
		deleteRequest.parent(request.param("parent"));
		deleteRequest.routing(request.param("routing"));
		deleteRequest.timeout(request.paramAsTime("timeout", DeleteRequest.DEFAULT_TIMEOUT));
		deleteRequest.refresh(request.paramAsBoolean("refresh", deleteRequest.refresh()));
		deleteRequest.version(RestActions.parseVersion(request));

		deleteRequest.listenerThreaded(false);

		deleteRequest.operationThreaded(true);
		deleteRequest.versionType(VersionType.fromString(request.param("version_type"), deleteRequest.versionType()));

		String replicationType = request.param("replication");
		if (replicationType != null) {
			deleteRequest.replicationType(ReplicationType.fromString(replicationType));
		}
		String consistencyLevel = request.param("consistency");
		if (consistencyLevel != null) {
			deleteRequest.consistencyLevel(WriteConsistencyLevel.fromString(consistencyLevel));
		}

		client.delete(deleteRequest, new ActionListener<DeleteResponse>() {
			@Override
			public void onResponse(DeleteResponse result) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject().field(Fields.OK, true).field(Fields.FOUND, !result.notFound())
							.field(Fields._INDEX, result.index()).field(Fields._TYPE, result.type())
							.field(Fields._ID, result.id()).field(Fields._VERSION, result.version()).endObject();
					RestStatus status = RestStatus.OK;
					if (result.notFound()) {
						status = RestStatus.NOT_FOUND;
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

		/** The Constant FOUND. */
		static final XContentBuilderString FOUND = new XContentBuilderString("found");

		/** The Constant _INDEX. */
		static final XContentBuilderString _INDEX = new XContentBuilderString("_index");

		/** The Constant _TYPE. */
		static final XContentBuilderString _TYPE = new XContentBuilderString("_type");

		/** The Constant _ID. */
		static final XContentBuilderString _ID = new XContentBuilderString("_id");

		/** The Constant _VERSION. */
		static final XContentBuilderString _VERSION = new XContentBuilderString("_version");
	}
}