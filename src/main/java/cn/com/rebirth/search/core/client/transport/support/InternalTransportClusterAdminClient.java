/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalTransportClusterAdminClient.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client.transport.support;

import java.util.Map;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.GenericAction;
import cn.com.rebirth.search.core.action.TransportActionNodeProxy;
import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.client.internal.InternalClusterAdminClient;
import cn.com.rebirth.search.core.client.support.AbstractClusterAdminClient;
import cn.com.rebirth.search.core.client.transport.TransportClientNodesService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.ImmutableMap;

/**
 * The Class InternalTransportClusterAdminClient.
 *
 * @author l.xue.nong
 */
public class InternalTransportClusterAdminClient extends AbstractClusterAdminClient implements
		InternalClusterAdminClient {

	/** The nodes service. */
	private final TransportClientNodesService nodesService;

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The actions. */
	private final ImmutableMap<ClusterAction, TransportActionNodeProxy> actions;

	/**
	 * Instantiates a new internal transport cluster admin client.
	 *
	 * @param settings the settings
	 * @param nodesService the nodes service
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param actions the actions
	 */
	@Inject
	public InternalTransportClusterAdminClient(Settings settings, TransportClientNodesService nodesService,
			ThreadPool threadPool, TransportService transportService, Map<String, GenericAction> actions) {
		this.nodesService = nodesService;
		this.threadPool = threadPool;
		MapBuilder<ClusterAction, TransportActionNodeProxy> actionsBuilder = new MapBuilder<ClusterAction, TransportActionNodeProxy>();
		for (GenericAction action : actions.values()) {
			if (action instanceof ClusterAction) {
				actionsBuilder.put((ClusterAction) action, new TransportActionNodeProxy(action, transportService));
			}
		}
		this.actions = actionsBuilder.immutableMap();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.internal.InternalClusterAdminClient#threadPool()
	 */
	@Override
	public ThreadPool threadPool() {
		return this.threadPool;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.ClusterAdminClient#execute(cn.com.rebirth.search.core.action.admin.cluster.ClusterAction, cn.com.rebirth.search.core.action.ActionRequest)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> ActionFuture<Response> execute(
			final ClusterAction<Request, Response, RequestBuilder> action, final Request request) {
		final TransportActionNodeProxy<Request, Response> proxy = actions.get(action);
		return nodesService.execute(new TransportClientNodesService.NodeCallback<ActionFuture<Response>>() {
			@Override
			public ActionFuture<Response> doWithNode(DiscoveryNode node) throws RebirthException {
				return proxy.execute(node, request);
			}
		});
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.ClusterAdminClient#execute(cn.com.rebirth.search.core.action.admin.cluster.ClusterAction, cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> void execute(
			final ClusterAction<Request, Response, RequestBuilder> action, final Request request,
			final ActionListener<Response> listener) {
		final TransportActionNodeProxy<Request, Response> proxy = actions.get(action);
		nodesService.execute(new TransportClientNodesService.NodeListenerCallback<Response>() {
			@Override
			public void doWithNode(DiscoveryNode node, ActionListener<Response> listener) throws RebirthException {
				proxy.execute(node, request, listener);
			}
		}, listener);
	}
}
