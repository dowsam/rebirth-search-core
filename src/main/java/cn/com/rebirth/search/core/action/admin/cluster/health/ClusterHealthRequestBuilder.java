/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterHealthRequestBuilder.java 2012-7-6 14:30:41 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.health;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.support.BaseClusterRequestBuilder;
import cn.com.rebirth.search.core.client.ClusterAdminClient;

/**
 * The Class ClusterHealthRequestBuilder.
 *
 * @author l.xue.nong
 */
public class ClusterHealthRequestBuilder extends BaseClusterRequestBuilder<ClusterHealthRequest, ClusterHealthResponse> {

	/**
	 * Instantiates a new cluster health request builder.
	 *
	 * @param clusterClient the cluster client
	 */
	public ClusterHealthRequestBuilder(ClusterAdminClient clusterClient) {
		super(clusterClient, new ClusterHealthRequest());
	}

	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the cluster health request builder
	 */
	public ClusterHealthRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the cluster health request builder
	 */
	public ClusterHealthRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the cluster health request builder
	 */
	public ClusterHealthRequestBuilder setTimeout(TimeValue timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the cluster health request builder
	 */
	public ClusterHealthRequestBuilder setTimeout(String timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the wait for status.
	 *
	 * @param waitForStatus the wait for status
	 * @return the cluster health request builder
	 */
	public ClusterHealthRequestBuilder setWaitForStatus(ClusterHealthStatus waitForStatus) {
		request.waitForStatus(waitForStatus);
		return this;
	}

	/**
	 * Sets the wait for green status.
	 *
	 * @return the cluster health request builder
	 */
	public ClusterHealthRequestBuilder setWaitForGreenStatus() {
		request.waitForGreenStatus();
		return this;
	}

	/**
	 * Sets the wait for yellow status.
	 *
	 * @return the cluster health request builder
	 */
	public ClusterHealthRequestBuilder setWaitForYellowStatus() {
		request.waitForYellowStatus();
		return this;
	}

	/**
	 * Sets the wait for relocating shards.
	 *
	 * @param waitForRelocatingShards the wait for relocating shards
	 * @return the cluster health request builder
	 */
	public ClusterHealthRequestBuilder setWaitForRelocatingShards(int waitForRelocatingShards) {
		request.waitForRelocatingShards(waitForRelocatingShards);
		return this;
	}

	/**
	 * Sets the wait for active shards.
	 *
	 * @param waitForActiveShards the wait for active shards
	 * @return the cluster health request builder
	 */
	public ClusterHealthRequestBuilder setWaitForActiveShards(int waitForActiveShards) {
		request.waitForActiveShards(waitForActiveShards);
		return this;
	}

	/**
	 * Sets the wait for nodes.
	 *
	 * @param waitForNodes the wait for nodes
	 * @return the cluster health request builder
	 */
	public ClusterHealthRequestBuilder setWaitForNodes(String waitForNodes) {
		request.waitForNodes(waitForNodes);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.cluster.support.BaseClusterRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<ClusterHealthResponse> listener) {
		client.health(request, listener);
	}
}
