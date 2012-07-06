/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterRerouteRequestBuilder.java 2012-3-29 15:01:08 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.reroute;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.support.BaseClusterRequestBuilder;
import cn.com.rebirth.search.core.client.ClusterAdminClient;


/**
 * The Class ClusterRerouteRequestBuilder.
 *
 * @author l.xue.nong
 */
public class ClusterRerouteRequestBuilder extends
		BaseClusterRequestBuilder<ClusterRerouteRequest, ClusterRerouteResponse> {

	
	/**
	 * Instantiates a new cluster reroute request builder.
	 *
	 * @param clusterClient the cluster client
	 */
	public ClusterRerouteRequestBuilder(ClusterAdminClient clusterClient) {
		super(clusterClient, new ClusterRerouteRequest());
	}

	
	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the cluster reroute request builder
	 */
	public ClusterRerouteRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	
	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the cluster reroute request builder
	 */
	public ClusterRerouteRequestBuilder setMasterNodeTimeout(String timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.cluster.support.BaseClusterRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<ClusterRerouteResponse> listener) {
		client.reroute(request, listener);
	}
}
