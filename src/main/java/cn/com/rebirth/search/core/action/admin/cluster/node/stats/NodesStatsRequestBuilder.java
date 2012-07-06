/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodesStatsRequestBuilder.java 2012-7-6 14:29:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.stats;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.support.BaseClusterRequestBuilder;
import cn.com.rebirth.search.core.client.ClusterAdminClient;

/**
 * The Class NodesStatsRequestBuilder.
 *
 * @author l.xue.nong
 */
public class NodesStatsRequestBuilder extends BaseClusterRequestBuilder<NodesStatsRequest, NodesStatsResponse> {

	/**
	 * Instantiates a new nodes stats request builder.
	 *
	 * @param clusterClient the cluster client
	 */
	public NodesStatsRequestBuilder(ClusterAdminClient clusterClient) {
		super(clusterClient, new NodesStatsRequest());
	}

	/**
	 * Sets the nodes ids.
	 *
	 * @param nodesIds the nodes ids
	 * @return the nodes stats request builder
	 */
	public NodesStatsRequestBuilder setNodesIds(String... nodesIds) {
		request.nodesIds(nodesIds);
		return this;
	}

	/**
	 * All.
	 *
	 * @return the nodes stats request builder
	 */
	public NodesStatsRequestBuilder all() {
		request.all();
		return this;
	}

	/**
	 * Clear.
	 *
	 * @return the nodes stats request builder
	 */
	public NodesStatsRequestBuilder clear() {
		request.clear();
		return this;
	}

	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the nodes stats request builder
	 */
	public NodesStatsRequestBuilder setIndices(boolean indices) {
		request.indices(indices);
		return this;
	}

	/**
	 * Sets the os.
	 *
	 * @param os the os
	 * @return the nodes stats request builder
	 */
	public NodesStatsRequestBuilder setOs(boolean os) {
		request.os(os);
		return this;
	}

	/**
	 * Sets the process.
	 *
	 * @param process the process
	 * @return the nodes stats request builder
	 */
	public NodesStatsRequestBuilder setProcess(boolean process) {
		request.process(process);
		return this;
	}

	/**
	 * Sets the jvm.
	 *
	 * @param jvm the jvm
	 * @return the nodes stats request builder
	 */
	public NodesStatsRequestBuilder setJvm(boolean jvm) {
		request.jvm(jvm);
		return this;
	}

	/**
	 * Sets the thread pool.
	 *
	 * @param threadPool the thread pool
	 * @return the nodes stats request builder
	 */
	public NodesStatsRequestBuilder setThreadPool(boolean threadPool) {
		request.threadPool(threadPool);
		return this;
	}

	/**
	 * Sets the network.
	 *
	 * @param network the network
	 * @return the nodes stats request builder
	 */
	public NodesStatsRequestBuilder setNetwork(boolean network) {
		request.network(network);
		return this;
	}

	/**
	 * Sets the fs.
	 *
	 * @param fs the fs
	 * @return the nodes stats request builder
	 */
	public NodesStatsRequestBuilder setFs(boolean fs) {
		request.fs(fs);
		return this;
	}

	/**
	 * Sets the transport.
	 *
	 * @param transport the transport
	 * @return the nodes stats request builder
	 */
	public NodesStatsRequestBuilder setTransport(boolean transport) {
		request.transport(transport);
		return this;
	}

	/**
	 * Sets the http.
	 *
	 * @param http the http
	 * @return the nodes stats request builder
	 */
	public NodesStatsRequestBuilder setHttp(boolean http) {
		request.http(http);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.cluster.support.BaseClusterRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<NodesStatsResponse> listener) {
		client.nodesStats(request, listener);
	}
}