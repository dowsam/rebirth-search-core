/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterAdminClient.java 2012-7-6 14:30:15 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client;

import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterHealthRequest;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterHealthRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterHealthResponse;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodesInfoRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodesInfoRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodesInfoResponse;
import cn.com.rebirth.search.core.action.admin.cluster.node.restart.NodesRestartRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.restart.NodesRestartRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.node.restart.NodesRestartResponse;
import cn.com.rebirth.search.core.action.admin.cluster.node.shutdown.NodesShutdownRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.shutdown.NodesShutdownRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.node.shutdown.NodesShutdownResponse;
import cn.com.rebirth.search.core.action.admin.cluster.node.stats.NodesStatsRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.stats.NodesStatsRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.node.stats.NodesStatsResponse;
import cn.com.rebirth.search.core.action.admin.cluster.reroute.ClusterRerouteRequest;
import cn.com.rebirth.search.core.action.admin.cluster.reroute.ClusterRerouteRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.reroute.ClusterRerouteResponse;
import cn.com.rebirth.search.core.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import cn.com.rebirth.search.core.action.admin.cluster.settings.ClusterUpdateSettingsRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.settings.ClusterUpdateSettingsResponse;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateRequest;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateRequestBuilder;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateResponse;

/**
 * The Interface ClusterAdminClient.
 *
 * @author l.xue.nong
 */
public interface ClusterAdminClient {

	/**
	 * Execute.
	 *
	 * @param <Request> the generic type
	 * @param <Response> the generic type
	 * @param <RequestBuilder> the generic type
	 * @param action the action
	 * @param request the request
	 * @return the action future
	 */
	<Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> ActionFuture<Response> execute(
			final ClusterAction<Request, Response, RequestBuilder> action, final Request request);

	/**
	 * Execute.
	 *
	 * @param <Request> the generic type
	 * @param <Response> the generic type
	 * @param <RequestBuilder> the generic type
	 * @param action the action
	 * @param request the request
	 * @param listener the listener
	 */
	<Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> void execute(
			final ClusterAction<Request, Response, RequestBuilder> action, final Request request,
			ActionListener<Response> listener);

	/**
	 * Prepare execute.
	 *
	 * @param <Request> the generic type
	 * @param <Response> the generic type
	 * @param <RequestBuilder> the generic type
	 * @param action the action
	 * @return the request builder
	 */
	<Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> RequestBuilder prepareExecute(
			final ClusterAction<Request, Response, RequestBuilder> action);

	/**
	 * Health.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<ClusterHealthResponse> health(ClusterHealthRequest request);

	/**
	 * Health.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void health(ClusterHealthRequest request, ActionListener<ClusterHealthResponse> listener);

	/**
	 * Prepare health.
	 *
	 * @param indices the indices
	 * @return the cluster health request builder
	 */
	ClusterHealthRequestBuilder prepareHealth(String... indices);

	/**
	 * State.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<ClusterStateResponse> state(ClusterStateRequest request);

	/**
	 * State.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void state(ClusterStateRequest request, ActionListener<ClusterStateResponse> listener);

	/**
	 * Prepare state.
	 *
	 * @return the cluster state request builder
	 */
	ClusterStateRequestBuilder prepareState();

	/**
	 * Update settings.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<ClusterUpdateSettingsResponse> updateSettings(ClusterUpdateSettingsRequest request);

	/**
	 * Update settings.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void updateSettings(ClusterUpdateSettingsRequest request, ActionListener<ClusterUpdateSettingsResponse> listener);

	/**
	 * Prepare update settings.
	 *
	 * @return the cluster update settings request builder
	 */
	ClusterUpdateSettingsRequestBuilder prepareUpdateSettings();

	/**
	 * Reroute.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<ClusterRerouteResponse> reroute(ClusterRerouteRequest request);

	/**
	 * Reroute.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void reroute(ClusterRerouteRequest request, ActionListener<ClusterRerouteResponse> listener);

	/**
	 * Prepare reroute.
	 *
	 * @return the cluster reroute request builder
	 */
	ClusterRerouteRequestBuilder prepareReroute();

	/**
	 * Nodes info.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<NodesInfoResponse> nodesInfo(NodesInfoRequest request);

	/**
	 * Nodes info.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void nodesInfo(NodesInfoRequest request, ActionListener<NodesInfoResponse> listener);

	/**
	 * Prepare nodes info.
	 *
	 * @param nodesIds the nodes ids
	 * @return the nodes info request builder
	 */
	NodesInfoRequestBuilder prepareNodesInfo(String... nodesIds);

	/**
	 * Nodes stats.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<NodesStatsResponse> nodesStats(NodesStatsRequest request);

	/**
	 * Nodes stats.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void nodesStats(NodesStatsRequest request, ActionListener<NodesStatsResponse> listener);

	/**
	 * Prepare nodes stats.
	 *
	 * @param nodesIds the nodes ids
	 * @return the nodes stats request builder
	 */
	NodesStatsRequestBuilder prepareNodesStats(String... nodesIds);

	/**
	 * Nodes shutdown.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<NodesShutdownResponse> nodesShutdown(NodesShutdownRequest request);

	/**
	 * Nodes shutdown.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void nodesShutdown(NodesShutdownRequest request, ActionListener<NodesShutdownResponse> listener);

	/**
	 * Prepare nodes shutdown.
	 *
	 * @param nodesIds the nodes ids
	 * @return the nodes shutdown request builder
	 */
	NodesShutdownRequestBuilder prepareNodesShutdown(String... nodesIds);

	/**
	 * Nodes restart.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<NodesRestartResponse> nodesRestart(NodesRestartRequest request);

	/**
	 * Nodes restart.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void nodesRestart(NodesRestartRequest request, ActionListener<NodesRestartResponse> listener);

	/**
	 * Prepare nodes restart.
	 *
	 * @param nodesIds the nodes ids
	 * @return the nodes restart request builder
	 */
	NodesRestartRequestBuilder prepareNodesRestart(String... nodesIds);
}
