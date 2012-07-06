/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AbstractClusterAdminClient.java 2012-3-29 15:01:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.client.support;

import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterHealthAction;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterHealthRequest;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterHealthRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterHealthResponse;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodesInfoAction;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodesInfoRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodesInfoRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodesInfoResponse;
import cn.com.rebirth.search.core.action.admin.cluster.node.restart.NodesRestartAction;
import cn.com.rebirth.search.core.action.admin.cluster.node.restart.NodesRestartRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.restart.NodesRestartRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.node.restart.NodesRestartResponse;
import cn.com.rebirth.search.core.action.admin.cluster.node.shutdown.NodesShutdownAction;
import cn.com.rebirth.search.core.action.admin.cluster.node.shutdown.NodesShutdownRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.shutdown.NodesShutdownRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.node.shutdown.NodesShutdownResponse;
import cn.com.rebirth.search.core.action.admin.cluster.node.stats.NodesStatsAction;
import cn.com.rebirth.search.core.action.admin.cluster.node.stats.NodesStatsRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.stats.NodesStatsRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.node.stats.NodesStatsResponse;
import cn.com.rebirth.search.core.action.admin.cluster.reroute.ClusterRerouteAction;
import cn.com.rebirth.search.core.action.admin.cluster.reroute.ClusterRerouteRequest;
import cn.com.rebirth.search.core.action.admin.cluster.reroute.ClusterRerouteRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.reroute.ClusterRerouteResponse;
import cn.com.rebirth.search.core.action.admin.cluster.settings.ClusterUpdateSettingsAction;
import cn.com.rebirth.search.core.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import cn.com.rebirth.search.core.action.admin.cluster.settings.ClusterUpdateSettingsRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.settings.ClusterUpdateSettingsResponse;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateAction;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateRequest;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateResponse;
import cn.com.rebirth.search.core.client.internal.InternalClusterAdminClient;


/**
 * The Class AbstractClusterAdminClient.
 *
 * @author l.xue.nong
 */
public abstract class AbstractClusterAdminClient implements InternalClusterAdminClient {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#prepareExecute(cn.com.summall.search.core.action.admin.cluster.ClusterAction)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> RequestBuilder prepareExecute(
			ClusterAction<Request, Response, RequestBuilder> action) {
		return action.newRequestBuilder(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#health(cn.com.summall.search.core.action.admin.cluster.health.ClusterHealthRequest)
	 */
	@Override
	public ActionFuture<ClusterHealthResponse> health(final ClusterHealthRequest request) {
		return execute(ClusterHealthAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#health(cn.com.summall.search.core.action.admin.cluster.health.ClusterHealthRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void health(final ClusterHealthRequest request, final ActionListener<ClusterHealthResponse> listener) {
		execute(ClusterHealthAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#prepareHealth(java.lang.String[])
	 */
	@Override
	public ClusterHealthRequestBuilder prepareHealth(String... indices) {
		return new ClusterHealthRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#state(cn.com.summall.search.core.action.admin.cluster.state.ClusterStateRequest)
	 */
	@Override
	public ActionFuture<ClusterStateResponse> state(final ClusterStateRequest request) {
		return execute(ClusterStateAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#state(cn.com.summall.search.core.action.admin.cluster.state.ClusterStateRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void state(final ClusterStateRequest request, final ActionListener<ClusterStateResponse> listener) {
		execute(ClusterStateAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#prepareState()
	 */
	@Override
	public ClusterStateRequestBuilder prepareState() {
		return new ClusterStateRequestBuilder(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#reroute(cn.com.summall.search.core.action.admin.cluster.reroute.ClusterRerouteRequest)
	 */
	@Override
	public ActionFuture<ClusterRerouteResponse> reroute(final ClusterRerouteRequest request) {
		return execute(ClusterRerouteAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#reroute(cn.com.summall.search.core.action.admin.cluster.reroute.ClusterRerouteRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void reroute(final ClusterRerouteRequest request, final ActionListener<ClusterRerouteResponse> listener) {
		execute(ClusterRerouteAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#prepareReroute()
	 */
	@Override
	public ClusterRerouteRequestBuilder prepareReroute() {
		return new ClusterRerouteRequestBuilder(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#updateSettings(cn.com.summall.search.core.action.admin.cluster.settings.ClusterUpdateSettingsRequest)
	 */
	@Override
	public ActionFuture<ClusterUpdateSettingsResponse> updateSettings(final ClusterUpdateSettingsRequest request) {
		return execute(ClusterUpdateSettingsAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#updateSettings(cn.com.summall.search.core.action.admin.cluster.settings.ClusterUpdateSettingsRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void updateSettings(final ClusterUpdateSettingsRequest request,
			final ActionListener<ClusterUpdateSettingsResponse> listener) {
		execute(ClusterUpdateSettingsAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#prepareUpdateSettings()
	 */
	@Override
	public ClusterUpdateSettingsRequestBuilder prepareUpdateSettings() {
		return new ClusterUpdateSettingsRequestBuilder(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#nodesInfo(cn.com.summall.search.core.action.admin.cluster.node.info.NodesInfoRequest)
	 */
	@Override
	public ActionFuture<NodesInfoResponse> nodesInfo(final NodesInfoRequest request) {
		return execute(NodesInfoAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#nodesInfo(cn.com.summall.search.core.action.admin.cluster.node.info.NodesInfoRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void nodesInfo(final NodesInfoRequest request, final ActionListener<NodesInfoResponse> listener) {
		execute(NodesInfoAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#prepareNodesInfo(java.lang.String[])
	 */
	@Override
	public NodesInfoRequestBuilder prepareNodesInfo(String... nodesIds) {
		return new NodesInfoRequestBuilder(this).setNodesIds(nodesIds);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#nodesStats(cn.com.summall.search.core.action.admin.cluster.node.stats.NodesStatsRequest)
	 */
	@Override
	public ActionFuture<NodesStatsResponse> nodesStats(final NodesStatsRequest request) {
		return execute(NodesStatsAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#nodesStats(cn.com.summall.search.core.action.admin.cluster.node.stats.NodesStatsRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void nodesStats(final NodesStatsRequest request, final ActionListener<NodesStatsResponse> listener) {
		execute(NodesStatsAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#prepareNodesStats(java.lang.String[])
	 */
	@Override
	public NodesStatsRequestBuilder prepareNodesStats(String... nodesIds) {
		return new NodesStatsRequestBuilder(this).setNodesIds(nodesIds);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#nodesRestart(cn.com.summall.search.core.action.admin.cluster.node.restart.NodesRestartRequest)
	 */
	@Override
	public ActionFuture<NodesRestartResponse> nodesRestart(final NodesRestartRequest request) {
		return execute(NodesRestartAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#nodesRestart(cn.com.summall.search.core.action.admin.cluster.node.restart.NodesRestartRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void nodesRestart(final NodesRestartRequest request, final ActionListener<NodesRestartResponse> listener) {
		execute(NodesRestartAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#prepareNodesRestart(java.lang.String[])
	 */
	@Override
	public NodesRestartRequestBuilder prepareNodesRestart(String... nodesIds) {
		return new NodesRestartRequestBuilder(this).setNodesIds(nodesIds);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#nodesShutdown(cn.com.summall.search.core.action.admin.cluster.node.shutdown.NodesShutdownRequest)
	 */
	@Override
	public ActionFuture<NodesShutdownResponse> nodesShutdown(final NodesShutdownRequest request) {
		return execute(NodesShutdownAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#nodesShutdown(cn.com.summall.search.core.action.admin.cluster.node.shutdown.NodesShutdownRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void nodesShutdown(final NodesShutdownRequest request, final ActionListener<NodesShutdownResponse> listener) {
		execute(NodesShutdownAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.ClusterAdminClient#prepareNodesShutdown(java.lang.String[])
	 */
	@Override
	public NodesShutdownRequestBuilder prepareNodesShutdown(String... nodesIds) {
		return new NodesShutdownRequestBuilder(this).setNodesIds(nodesIds);
	}
}
