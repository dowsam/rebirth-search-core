/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodesInfoRequestBuilder.java 2012-7-6 14:28:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.info;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.support.BaseClusterRequestBuilder;
import cn.com.rebirth.search.core.client.ClusterAdminClient;

/**
 * The Class NodesInfoRequestBuilder.
 *
 * @author l.xue.nong
 */
public class NodesInfoRequestBuilder extends BaseClusterRequestBuilder<NodesInfoRequest, NodesInfoResponse> {

	/**
	 * Instantiates a new nodes info request builder.
	 *
	 * @param clusterClient the cluster client
	 */
	public NodesInfoRequestBuilder(ClusterAdminClient clusterClient) {
		super(clusterClient, new NodesInfoRequest());
	}

	/**
	 * Sets the nodes ids.
	 *
	 * @param nodesIds the nodes ids
	 * @return the nodes info request builder
	 */
	public NodesInfoRequestBuilder setNodesIds(String... nodesIds) {
		request.nodesIds(nodesIds);
		return this;
	}

	/**
	 * Clear.
	 *
	 * @return the nodes info request builder
	 */
	public NodesInfoRequestBuilder clear() {
		request.clear();
		return this;
	}

	/**
	 * All.
	 *
	 * @return the nodes info request builder
	 */
	public NodesInfoRequestBuilder all() {
		request.all();
		return this;
	}

	/**
	 * Sets the settings.
	 *
	 * @param settings the settings
	 * @return the nodes info request builder
	 */
	public NodesInfoRequestBuilder setSettings(boolean settings) {
		request.settings(settings);
		return this;
	}

	/**
	 * Sets the os.
	 *
	 * @param os the os
	 * @return the nodes info request builder
	 */
	public NodesInfoRequestBuilder setOs(boolean os) {
		request.os(os);
		return this;
	}

	/**
	 * Sets the process.
	 *
	 * @param process the process
	 * @return the nodes info request builder
	 */
	public NodesInfoRequestBuilder setProcess(boolean process) {
		request.process(process);
		return this;
	}

	/**
	 * Sets the jvm.
	 *
	 * @param jvm the jvm
	 * @return the nodes info request builder
	 */
	public NodesInfoRequestBuilder setJvm(boolean jvm) {
		request.jvm(jvm);
		return this;
	}

	/**
	 * Sets the thread pool.
	 *
	 * @param threadPool the thread pool
	 * @return the nodes info request builder
	 */
	public NodesInfoRequestBuilder setThreadPool(boolean threadPool) {
		request.threadPool(threadPool);
		return this;
	}

	/**
	 * Sets the network.
	 *
	 * @param network the network
	 * @return the nodes info request builder
	 */
	public NodesInfoRequestBuilder setNetwork(boolean network) {
		request.network(network);
		return this;
	}

	/**
	 * Sets the transport.
	 *
	 * @param transport the transport
	 * @return the nodes info request builder
	 */
	public NodesInfoRequestBuilder setTransport(boolean transport) {
		request.transport(transport);
		return this;
	}

	/**
	 * Sets the http.
	 *
	 * @param http the http
	 * @return the nodes info request builder
	 */
	public NodesInfoRequestBuilder setHttp(boolean http) {
		request.http(http);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.cluster.support.BaseClusterRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<NodesInfoResponse> listener) {
		client.nodesInfo(request, listener);
	}
}
