/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PutIndexTemplateRequestBuilder.java 2012-3-29 15:02:19 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.template.put;

import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class PutIndexTemplateRequestBuilder.
 *
 * @author l.xue.nong
 */
public class PutIndexTemplateRequestBuilder extends
		BaseIndicesRequestBuilder<PutIndexTemplateRequest, PutIndexTemplateResponse> {

	
	/**
	 * Instantiates a new put index template request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public PutIndexTemplateRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new PutIndexTemplateRequest());
	}

	
	/**
	 * Instantiates a new put index template request builder.
	 *
	 * @param indicesClient the indices client
	 * @param name the name
	 */
	public PutIndexTemplateRequestBuilder(IndicesAdminClient indicesClient, String name) {
		super(indicesClient, new PutIndexTemplateRequest(name));
	}

	
	/**
	 * Sets the template.
	 *
	 * @param template the template
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder setTemplate(String template) {
		request.template(template);
		return this;
	}

	
	/**
	 * Sets the order.
	 *
	 * @param order the order
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder setOrder(int order) {
		request.order(order);
		return this;
	}

	
	/**
	 * Sets the create.
	 *
	 * @param create the create
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder setCreate(boolean create) {
		request.create(create);
		return this;
	}

	
	/**
	 * Sets the settings.
	 *
	 * @param settings the settings
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder setSettings(Settings settings) {
		request.settings(settings);
		return this;
	}

	
	/**
	 * Sets the settings.
	 *
	 * @param settings the settings
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder setSettings(Settings.Builder settings) {
		request.settings(settings);
		return this;
	}

	
	/**
	 * Sets the settings.
	 *
	 * @param source the source
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder setSettings(String source) {
		request.settings(source);
		return this;
	}

	
	/**
	 * Sets the settings.
	 *
	 * @param source the source
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder setSettings(Map<String, Object> source) {
		request.settings(source);
		return this;
	}

	
	/**
	 * Adds the mapping.
	 *
	 * @param type the type
	 * @param source the source
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder addMapping(String type, String source) {
		request.mapping(type, source);
		return this;
	}

	
	/**
	 * Cause.
	 *
	 * @param cause the cause
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder cause(String cause) {
		request.cause(cause);
		return this;
	}

	
	/**
	 * Adds the mapping.
	 *
	 * @param type the type
	 * @param source the source
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder addMapping(String type, XContentBuilder source) {
		request.mapping(type, source);
		return this;
	}

	
	/**
	 * Adds the mapping.
	 *
	 * @param type the type
	 * @param source the source
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder addMapping(String type, Map<String, Object> source) {
		request.mapping(type, source);
		return this;
	}

	
	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder setTimeout(TimeValue timeout) {
		request.timeout(timeout);
		return this;
	}

	
	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder setTimeout(String timeout) {
		request.timeout(timeout);
		return this;
	}

	
	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	
	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the put index template request builder
	 */
	public PutIndexTemplateRequestBuilder setMasterNodeTimeout(String timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<PutIndexTemplateResponse> listener) {
		client.putTemplate(request, listener);
	}
}
