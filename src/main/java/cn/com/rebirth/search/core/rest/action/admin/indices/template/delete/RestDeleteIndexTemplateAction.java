/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestDeleteIndexTemplateAction.java 2012-7-6 14:29:00 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.template.delete;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import cn.com.rebirth.search.core.action.admin.indices.template.delete.DeleteIndexTemplateResponse;
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
 * The Class RestDeleteIndexTemplateAction.
 *
 * @author l.xue.nong
 */
public class RestDeleteIndexTemplateAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest delete index template action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestDeleteIndexTemplateAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(RestRequest.Method.DELETE, "/_template/{name}", this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		DeleteIndexTemplateRequest deleteIndexTemplateRequest = new DeleteIndexTemplateRequest(request.param("name"));
		deleteIndexTemplateRequest.timeout(request.paramAsTime("timeout", TimeValue.timeValueSeconds(10)));
		client.admin().indices()
				.deleteTemplate(deleteIndexTemplateRequest, new ActionListener<DeleteIndexTemplateResponse>() {
					@Override
					public void onResponse(DeleteIndexTemplateResponse response) {
						try {
							XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
							builder.startObject().field(Fields.OK, true)
									.field(Fields.ACKNOWLEDGED, response.acknowledged()).endObject();
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