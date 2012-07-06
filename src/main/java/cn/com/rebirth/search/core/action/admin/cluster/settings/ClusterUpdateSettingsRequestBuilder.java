/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterUpdateSettingsRequestBuilder.java 2012-3-29 15:02:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.settings;

import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.support.BaseClusterRequestBuilder;
import cn.com.rebirth.search.core.client.ClusterAdminClient;


/**
 * The Class ClusterUpdateSettingsRequestBuilder.
 *
 * @author l.xue.nong
 */
public class ClusterUpdateSettingsRequestBuilder extends
		BaseClusterRequestBuilder<ClusterUpdateSettingsRequest, ClusterUpdateSettingsResponse> {

	
	/**
	 * Instantiates a new cluster update settings request builder.
	 *
	 * @param clusterClient the cluster client
	 */
	public ClusterUpdateSettingsRequestBuilder(ClusterAdminClient clusterClient) {
		super(clusterClient, new ClusterUpdateSettingsRequest());
	}

	
	/**
	 * Sets the transient settings.
	 *
	 * @param settings the settings
	 * @return the cluster update settings request builder
	 */
	public ClusterUpdateSettingsRequestBuilder setTransientSettings(Settings settings) {
		request.transientSettings(settings);
		return this;
	}

	
	/**
	 * Sets the transient settings.
	 *
	 * @param settings the settings
	 * @return the cluster update settings request builder
	 */
	public ClusterUpdateSettingsRequestBuilder setTransientSettings(Settings.Builder settings) {
		request.transientSettings(settings);
		return this;
	}

	
	/**
	 * Sets the transient settings.
	 *
	 * @param settings the settings
	 * @return the cluster update settings request builder
	 */
	public ClusterUpdateSettingsRequestBuilder setTransientSettings(String settings) {
		request.transientSettings(settings);
		return this;
	}

	
	/**
	 * Sets the transient settings.
	 *
	 * @param settings the settings
	 * @return the cluster update settings request builder
	 */
	public ClusterUpdateSettingsRequestBuilder setTransientSettings(Map settings) {
		request.transientSettings(settings);
		return this;
	}

	
	/**
	 * Sets the persistent settings.
	 *
	 * @param settings the settings
	 * @return the cluster update settings request builder
	 */
	public ClusterUpdateSettingsRequestBuilder setPersistentSettings(Settings settings) {
		request.persistentSettings(settings);
		return this;
	}

	
	/**
	 * Sets the persistent settings.
	 *
	 * @param settings the settings
	 * @return the cluster update settings request builder
	 */
	public ClusterUpdateSettingsRequestBuilder setPersistentSettings(Settings.Builder settings) {
		request.persistentSettings(settings);
		return this;
	}

	
	/**
	 * Sets the persistent settings.
	 *
	 * @param settings the settings
	 * @return the cluster update settings request builder
	 */
	public ClusterUpdateSettingsRequestBuilder setPersistentSettings(String settings) {
		request.persistentSettings(settings);
		return this;
	}

	
	/**
	 * Sets the persistent settings.
	 *
	 * @param settings the settings
	 * @return the cluster update settings request builder
	 */
	public ClusterUpdateSettingsRequestBuilder setPersistentSettings(Map settings) {
		request.persistentSettings(settings);
		return this;
	}

	
	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the cluster update settings request builder
	 */
	public ClusterUpdateSettingsRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	
	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the cluster update settings request builder
	 */
	public ClusterUpdateSettingsRequestBuilder setMasterNodeTimeout(String timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.cluster.support.BaseClusterRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<ClusterUpdateSettingsResponse> listener) {
		client.updateSettings(request, listener);
	}
}
