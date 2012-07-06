/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodesShutdownRequestBuilder.java 2012-7-6 14:28:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.shutdown;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.support.BaseClusterRequestBuilder;
import cn.com.rebirth.search.core.client.ClusterAdminClient;

/**
 * The Class NodesShutdownRequestBuilder.
 *
 * @author l.xue.nong
 */
public class NodesShutdownRequestBuilder extends BaseClusterRequestBuilder<NodesShutdownRequest, NodesShutdownResponse> {

	/**
	 * Instantiates a new nodes shutdown request builder.
	 *
	 * @param clusterClient the cluster client
	 */
	public NodesShutdownRequestBuilder(ClusterAdminClient clusterClient) {
		super(clusterClient, new NodesShutdownRequest());
	}

	/**
	 * Sets the nodes ids.
	 *
	 * @param nodesIds the nodes ids
	 * @return the nodes shutdown request builder
	 */
	public NodesShutdownRequestBuilder setNodesIds(String... nodesIds) {
		request.nodesIds(nodesIds);
		return this;
	}

	/**
	 * Sets the delay.
	 *
	 * @param delay the delay
	 * @return the nodes shutdown request builder
	 */
	public NodesShutdownRequestBuilder setDelay(TimeValue delay) {
		request.delay(delay);
		return this;
	}

	/**
	 * Sets the delay.
	 *
	 * @param delay the delay
	 * @return the nodes shutdown request builder
	 */
	public NodesShutdownRequestBuilder setDelay(String delay) {
		request.delay(delay);
		return this;
	}

	/**
	 * Sets the exit.
	 *
	 * @param exit the exit
	 * @return the nodes shutdown request builder
	 */
	public NodesShutdownRequestBuilder setExit(boolean exit) {
		request.exit(exit);
		return this;
	}

	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the nodes shutdown request builder
	 */
	public NodesShutdownRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.cluster.support.BaseClusterRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<NodesShutdownResponse> listener) {
		client.nodesShutdown(request, listener);
	}
}