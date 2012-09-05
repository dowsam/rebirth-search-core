/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalTransportIndicesAdminClient.java 2012-7-6 14:29:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client.transport.support;

import java.util.Map;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.GenericAction;
import cn.com.rebirth.search.core.action.TransportActionNodeProxy;
import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;
import cn.com.rebirth.search.core.client.support.AbstractIndicesAdminClient;
import cn.com.rebirth.search.core.client.transport.TransportClientNodesService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.ImmutableMap;

/**
 * The Class InternalTransportIndicesAdminClient.
 *
 * @author l.xue.nong
 */
public class InternalTransportIndicesAdminClient extends AbstractIndicesAdminClient implements IndicesAdminClient {

	/** The nodes service. */
	private final TransportClientNodesService nodesService;

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The actions. */
	private final ImmutableMap<IndicesAction, TransportActionNodeProxy> actions;

	/**
	 * Instantiates a new internal transport indices admin client.
	 *
	 * @param settings the settings
	 * @param nodesService the nodes service
	 * @param transportService the transport service
	 * @param threadPool the thread pool
	 * @param actions the actions
	 */
	@Inject
	public InternalTransportIndicesAdminClient(Settings settings, TransportClientNodesService nodesService,
			TransportService transportService, ThreadPool threadPool, Map<String, GenericAction> actions) {
		this.nodesService = nodesService;
		this.threadPool = threadPool;
		MapBuilder<IndicesAction, TransportActionNodeProxy> actionsBuilder = new MapBuilder<IndicesAction, TransportActionNodeProxy>();
		for (GenericAction action : actions.values()) {
			if (action instanceof IndicesAction) {
				actionsBuilder.put((IndicesAction) action, new TransportActionNodeProxy(action, transportService));
			}
		}
		this.actions = actionsBuilder.immutableMap();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.internal.InternalIndicesAdminClient#threadPool()
	 */
	@Override
	public ThreadPool threadPool() {
		return this.threadPool;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.IndicesAdminClient#execute(cn.com.rebirth.search.core.action.admin.indices.IndicesAction, cn.com.rebirth.search.core.action.ActionRequest)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> ActionFuture<Response> execute(
			final IndicesAction<Request, Response, RequestBuilder> action, final Request request) {
		final TransportActionNodeProxy<Request, Response> proxy = actions.get(action);
		return nodesService.execute(new TransportClientNodesService.NodeCallback<ActionFuture<Response>>() {
			@Override
			public ActionFuture<Response> doWithNode(DiscoveryNode node) throws RebirthException {
				return proxy.execute(node, request);
			}
		});
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.IndicesAdminClient#execute(cn.com.rebirth.search.core.action.admin.indices.IndicesAction, cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> void execute(
			final IndicesAction<Request, Response, RequestBuilder> action, final Request request,
			ActionListener<Response> listener) {
		final TransportActionNodeProxy<Request, Response> proxy = actions.get(action);
		nodesService.execute(new TransportClientNodesService.NodeListenerCallback<Response>() {
			@Override
			public void doWithNode(DiscoveryNode node, ActionListener<Response> listener) throws RebirthException {
				proxy.execute(node, request, listener);
			}
		}, listener);
	}
}
