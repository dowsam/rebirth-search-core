/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CreateIndexRequestBuilder.java 2012-3-29 15:01:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.create;

import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class CreateIndexRequestBuilder.
 *
 * @author l.xue.nong
 */
public class CreateIndexRequestBuilder extends BaseIndicesRequestBuilder<CreateIndexRequest, CreateIndexResponse> {

	
	/**
	 * Instantiates a new creates the index request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public CreateIndexRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new CreateIndexRequest());
	}

	
	/**
	 * Instantiates a new creates the index request builder.
	 *
	 * @param indicesClient the indices client
	 * @param index the index
	 */
	public CreateIndexRequestBuilder(IndicesAdminClient indicesClient, String index) {
		super(indicesClient, new CreateIndexRequest(index));
	}

	
	/**
	 * Sets the index.
	 *
	 * @param index the index
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder setIndex(String index) {
		request.index(index);
		return this;
	}

	
	/**
	 * Sets the settings.
	 *
	 * @param settings the settings
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder setSettings(Settings settings) {
		request.settings(settings);
		return this;
	}

	
	/**
	 * Sets the settings.
	 *
	 * @param settings the settings
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder setSettings(Settings.Builder settings) {
		request.settings(settings);
		return this;
	}

	
	/**
	 * Sets the settings.
	 *
	 * @param builder the builder
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder setSettings(XContentBuilder builder) {
		request.settings(builder);
		return this;
	}

	
	/**
	 * Sets the settings.
	 *
	 * @param source the source
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder setSettings(String source) {
		request.settings(source);
		return this;
	}

	
	/**
	 * Sets the settings.
	 *
	 * @param source the source
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder setSettings(Map<String, Object> source) {
		request.settings(source);
		return this;
	}

	
	/**
	 * Adds the mapping.
	 *
	 * @param type the type
	 * @param source the source
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder addMapping(String type, String source) {
		request.mapping(type, source);
		return this;
	}

	
	/**
	 * Cause.
	 *
	 * @param cause the cause
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder cause(String cause) {
		request.cause(cause);
		return this;
	}

	
	/**
	 * Adds the mapping.
	 *
	 * @param type the type
	 * @param source the source
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder addMapping(String type, XContentBuilder source) {
		request.mapping(type, source);
		return this;
	}

	
	/**
	 * Adds the mapping.
	 *
	 * @param type the type
	 * @param source the source
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder addMapping(String type, Map<String, Object> source) {
		request.mapping(type, source);
		return this;
	}

	
	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder setTimeout(TimeValue timeout) {
		request.timeout(timeout);
		return this;
	}

	
	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder setTimeout(String timeout) {
		request.timeout(timeout);
		return this;
	}

	
	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	
	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the creates the index request builder
	 */
	public CreateIndexRequestBuilder setMasterNodeTimeout(String timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<CreateIndexResponse> listener) {
		client.create(request, listener);
	}
}
