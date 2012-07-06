/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core UpdateSettingsRequestBuilder.java 2012-3-29 15:01:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.settings;

import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class UpdateSettingsRequestBuilder.
 *
 * @author l.xue.nong
 */
public class UpdateSettingsRequestBuilder extends
		BaseIndicesRequestBuilder<UpdateSettingsRequest, UpdateSettingsResponse> {

	
	/**
	 * Instantiates a new update settings request builder.
	 *
	 * @param indicesClient the indices client
	 * @param indices the indices
	 */
	public UpdateSettingsRequestBuilder(IndicesAdminClient indicesClient, String... indices) {
		super(indicesClient, new UpdateSettingsRequest(indices));
	}

	
	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the update settings request builder
	 */
	public UpdateSettingsRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	
	/**
	 * Sets the settings.
	 *
	 * @param settings the settings
	 * @return the update settings request builder
	 */
	public UpdateSettingsRequestBuilder setSettings(Settings settings) {
		request.settings(settings);
		return this;
	}

	
	/**
	 * Sets the settings.
	 *
	 * @param settings the settings
	 * @return the update settings request builder
	 */
	public UpdateSettingsRequestBuilder setSettings(Settings.Builder settings) {
		request.settings(settings);
		return this;
	}

	
	/**
	 * Sets the settings.
	 *
	 * @param source the source
	 * @return the update settings request builder
	 */
	public UpdateSettingsRequestBuilder setSettings(String source) {
		request.settings(source);
		return this;
	}

	
	/**
	 * Sets the settings.
	 *
	 * @param source the source
	 * @return the update settings request builder
	 */
	public UpdateSettingsRequestBuilder setSettings(Map<String, Object> source) {
		request.settings(source);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<UpdateSettingsResponse> listener) {
		client.updateSettings(request, listener);
	}
}
