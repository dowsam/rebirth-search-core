/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterStateRequestBuilder.java 2012-3-29 15:01:29 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.state;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.support.BaseClusterRequestBuilder;
import cn.com.rebirth.search.core.client.ClusterAdminClient;


/**
 * The Class ClusterStateRequestBuilder.
 *
 * @author l.xue.nong
 */
public class ClusterStateRequestBuilder extends BaseClusterRequestBuilder<ClusterStateRequest, ClusterStateResponse> {

	
	/**
	 * Instantiates a new cluster state request builder.
	 *
	 * @param clusterClient the cluster client
	 */
	public ClusterStateRequestBuilder(ClusterAdminClient clusterClient) {
		super(clusterClient, new ClusterStateRequest());
	}

	
	/**
	 * Sets the filter all.
	 *
	 * @return the cluster state request builder
	 */
	public ClusterStateRequestBuilder setFilterAll() {
		request.filterAll();
		return this;
	}

	
	/**
	 * Sets the filter blocks.
	 *
	 * @param filter the filter
	 * @return the cluster state request builder
	 */
	public ClusterStateRequestBuilder setFilterBlocks(boolean filter) {
		request.filterBlocks(filter);
		return this;
	}

	
	/**
	 * Sets the filter meta data.
	 *
	 * @param filter the filter
	 * @return the cluster state request builder
	 */
	public ClusterStateRequestBuilder setFilterMetaData(boolean filter) {
		request.filterMetaData(filter);
		return this;
	}

	
	/**
	 * Sets the filter nodes.
	 *
	 * @param filter the filter
	 * @return the cluster state request builder
	 */
	public ClusterStateRequestBuilder setFilterNodes(boolean filter) {
		request.filterNodes(filter);
		return this;
	}

	
	/**
	 * Sets the filter routing table.
	 *
	 * @param filter the filter
	 * @return the cluster state request builder
	 */
	public ClusterStateRequestBuilder setFilterRoutingTable(boolean filter) {
		request.filterRoutingTable(filter);
		return this;
	}

	
	/**
	 * Sets the filter indices.
	 *
	 * @param indices the indices
	 * @return the cluster state request builder
	 */
	public ClusterStateRequestBuilder setFilterIndices(String... indices) {
		request.filteredIndices(indices);
		return this;
	}

	
	/**
	 * Sets the filter index templates.
	 *
	 * @param templates the templates
	 * @return the cluster state request builder
	 */
	public ClusterStateRequestBuilder setFilterIndexTemplates(String... templates) {
		request.filteredIndexTemplates(templates);
		return this;
	}

	
	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the cluster state request builder
	 */
	public ClusterStateRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	
	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the cluster state request builder
	 */
	public ClusterStateRequestBuilder setMasterNodeTimeout(String timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	
	/**
	 * Sets the local.
	 *
	 * @param local the local
	 * @return the cluster state request builder
	 */
	public ClusterStateRequestBuilder setLocal(boolean local) {
		request.local(local);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.cluster.support.BaseClusterRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<ClusterStateResponse> listener) {
		client.state(request, listener);
	}
}
