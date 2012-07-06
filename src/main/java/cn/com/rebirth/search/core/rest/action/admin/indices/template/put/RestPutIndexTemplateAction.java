/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestPutIndexTemplateAction.java 2012-3-29 15:01:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest.action.admin.indices.template.put;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.template.put.PutIndexTemplateRequest;
import cn.com.rebirth.search.core.action.admin.indices.template.put.PutIndexTemplateResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestHandler;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;


/**
 * The Class RestPutIndexTemplateAction.
 *
 * @author l.xue.nong
 */
public class RestPutIndexTemplateAction extends BaseRestHandler {

	
	/**
	 * Instantiates a new rest put index template action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestPutIndexTemplateAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(RestRequest.Method.PUT, "/_template/{name}", this);
		controller.registerHandler(RestRequest.Method.POST, "/_template/{name}", new CreateHandler());
	}

	
	/**
	 * The Class CreateHandler.
	 *
	 * @author l.xue.nong
	 */
	final class CreateHandler implements RestHandler {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(RestRequest request, RestChannel channel) {
			request.params().put("create", "true");
			RestPutIndexTemplateAction.this.handleRequest(request, channel);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		PutIndexTemplateRequest putRequest = new PutIndexTemplateRequest(request.param("name"));

		try {
			putRequest.create(request.paramAsBoolean("create", false));
			putRequest.cause(request.param("cause", ""));
			putRequest.timeout(request.paramAsTime("timeout", TimeValue.timeValueSeconds(10)));

			
			Map<String, Object> source = XContentFactory
					.xContent(request.contentByteArray(), request.contentByteArrayOffset(), request.contentLength())
					.createParser(request.contentByteArray(), request.contentByteArrayOffset(), request.contentLength())
					.mapOrderedAndClose();

			if (source.containsKey("template")) {
				putRequest.template(source.get("template").toString());
			}
			if (source.containsKey("order")) {
				putRequest.order(XContentMapValues.nodeIntegerValue(source.get("order"), putRequest.order()));
			}
			if (source.containsKey("settings")) {
				if (!(source.get("settings") instanceof Map)) {
					throw new RestartIllegalArgumentException(
							"Malformed settings section, should include an inner object");
				}
				putRequest.settings((Map<String, Object>) source.get("settings"));
			}
			if (source.containsKey("mappings")) {
				Map<String, Object> mappings = (Map<String, Object>) source.get("mappings");
				for (Map.Entry<String, Object> entry : mappings.entrySet()) {
					if (!(entry.getValue() instanceof Map)) {
						throw new RestartIllegalArgumentException("Malformed mappings section for type ["
								+ entry.getKey() + "], should include an inner object describing the mapping");
					}
					putRequest.mapping(entry.getKey(), (Map<String, Object>) entry.getValue());
				}
			}
		} catch (Exception e) {
			try {
				channel.sendResponse(new XContentThrowableRestResponse(request, e));
			} catch (IOException e1) {
				logger.warn("Failed to send response", e1);
			}
			return;
		}

		putRequest.template(request.param("template", putRequest.template()));
		putRequest.order(request.paramAsInt("order", putRequest.order()));

		client.admin().indices().putTemplate(putRequest, new ActionListener<PutIndexTemplateResponse>() {
			@Override
			public void onResponse(PutIndexTemplateResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject().field(Fields.OK, true).field(Fields.ACKNOWLEDGED, response.acknowledged())
							.endObject();
					channel.sendResponse(new XContentRestResponse(request, RestStatus.OK, builder));
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