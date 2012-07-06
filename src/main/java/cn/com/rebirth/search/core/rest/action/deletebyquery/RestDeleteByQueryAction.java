/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestDeleteByQueryAction.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.deletebyquery;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.BytesStream;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryRequest;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryResponse;
import cn.com.rebirth.search.core.action.deletebyquery.IndexDeleteByQueryResponse;
import cn.com.rebirth.search.core.action.deletebyquery.ShardDeleteByQueryRequest;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.RestRequest.Method;
import cn.com.rebirth.search.core.rest.action.support.RestActions;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.RestStatus.PRECONDITION_FAILED;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.splitIndices;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.splitTypes;

/**
 * The Class RestDeleteByQueryAction.
 *
 * @author l.xue.nong
 */
public class RestDeleteByQueryAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest delete by query action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestDeleteByQueryAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(Method.DELETE, "/{index}/_query", this);
		controller.registerHandler(Method.DELETE, "/{index}/{type}/_query", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(splitIndices(request.param("index")));

		deleteByQueryRequest.listenerThreaded(false);
		try {
			if (request.hasContent()) {
				deleteByQueryRequest.query(request.contentByteArray(), request.contentByteArrayOffset(),
						request.contentLength(), request.contentUnsafe());
			} else {
				String source = request.param("source");
				if (source != null) {
					deleteByQueryRequest.query(source);
				} else {
					BytesStream bytes = RestActions.parseQuerySource(request);
					deleteByQueryRequest.query(bytes.underlyingBytes(), 0, bytes.size(), false);
				}
			}
			deleteByQueryRequest.types(splitTypes(request.param("type")));
			deleteByQueryRequest.timeout(request.paramAsTime("timeout", ShardDeleteByQueryRequest.DEFAULT_TIMEOUT));

			deleteByQueryRequest.routing(request.param("routing"));
			String replicationType = request.param("replication");
			if (replicationType != null) {
				deleteByQueryRequest.replicationType(ReplicationType.fromString(replicationType));
			}
			String consistencyLevel = request.param("consistency");
			if (consistencyLevel != null) {
				deleteByQueryRequest.consistencyLevel(WriteConsistencyLevel.fromString(consistencyLevel));
			}
		} catch (Exception e) {
			try {
				XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
				channel.sendResponse(new XContentRestResponse(request, PRECONDITION_FAILED, builder.startObject()
						.field("error", e.getMessage()).endObject()));
			} catch (IOException e1) {
				logger.error("Failed to send failure response", e1);
			}
			return;
		}
		client.deleteByQuery(deleteByQueryRequest, new ActionListener<DeleteByQueryResponse>() {
			@Override
			public void onResponse(DeleteByQueryResponse result) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject().field("ok", true);

					builder.startObject("_indices");
					for (IndexDeleteByQueryResponse indexDeleteByQueryResponse : result.indices().values()) {
						builder.startObject(indexDeleteByQueryResponse.index(),
								XContentBuilder.FieldCaseConversion.NONE);

						builder.startObject("_shards");
						builder.field("total", indexDeleteByQueryResponse.totalShards());
						builder.field("successful", indexDeleteByQueryResponse.successfulShards());
						builder.field("failed", indexDeleteByQueryResponse.failedShards());
						builder.endObject();

						builder.endObject();
					}
					builder.endObject();

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