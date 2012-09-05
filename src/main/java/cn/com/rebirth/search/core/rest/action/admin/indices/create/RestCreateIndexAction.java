/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestCreateIndexAction.java 2012-7-6 14:29:30 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.create;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.settings.SettingsException;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentType;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexResponse;
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
 * The Class RestCreateIndexAction.
 *
 * @author l.xue.nong
 */
public class RestCreateIndexAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest create index action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestCreateIndexAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(RestRequest.Method.PUT, "/{index}", this);
		controller.registerHandler(RestRequest.Method.POST, "/{index}", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		CreateIndexRequest createIndexRequest = new CreateIndexRequest(request.param("index"));
		if (request.hasContent()) {
			XContentType xContentType = XContentFactory.xContentType(request.contentByteArray(),
					request.contentByteArrayOffset(), request.contentLength());
			if (xContentType != null) {
				try {
					Map<String, Object> source = XContentFactory
							.xContent(xContentType)
							.createParser(request.contentByteArray(), request.contentByteArrayOffset(),
									request.contentLength()).mapAndClose();
					boolean found = false;
					if (source.containsKey("settings")) {
						createIndexRequest.settings((Map<String, Object>) source.get("settings"));
						found = true;
					}
					if (source.containsKey("mappings")) {
						found = true;
						Map<String, Object> mappings = (Map<String, Object>) source.get("mappings");
						for (Map.Entry<String, Object> entry : mappings.entrySet()) {
							createIndexRequest.mapping(entry.getKey(), (Map<String, Object>) entry.getValue());
						}
					}
					if (!found) {

						createIndexRequest.settings(source);
					}
				} catch (Exception e) {
					try {
						channel.sendResponse(new XContentThrowableRestResponse(request, e));
					} catch (IOException e1) {
						logger.warn("Failed to send response", e1);
					}
					return;
				}
			} else {

				try {
					createIndexRequest.settings(request.contentAsString());
				} catch (Exception e) {
					try {
						channel.sendResponse(new XContentThrowableRestResponse(request, RestStatus.BAD_REQUEST,
								new SettingsException("Failed to parse index settings", e)));
					} catch (IOException e1) {
						logger.warn("Failed to send response", e1);
					}
					return;
				}
			}
		}

		createIndexRequest.timeout(request.paramAsTime("timeout", TimeValue.timeValueSeconds(10)));

		client.admin().indices().create(createIndexRequest, new ActionListener<CreateIndexResponse>() {
			@Override
			public void onResponse(CreateIndexResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject().field("ok", true).field("acknowledged", response.acknowledged()).endObject();
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
}
