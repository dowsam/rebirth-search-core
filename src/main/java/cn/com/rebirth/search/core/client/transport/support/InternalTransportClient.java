/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalTransportClient.java 2012-3-29 15:01:47 l.xue.nong$$
 */


package cn.com.rebirth.search.core.client.transport.support;

import java.util.Map;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.GenericAction;
import cn.com.rebirth.search.core.action.TransportActionNodeProxy;
import cn.com.rebirth.search.core.client.AdminClient;
import cn.com.rebirth.search.core.client.internal.InternalClient;
import cn.com.rebirth.search.core.client.support.AbstractClient;
import cn.com.rebirth.search.core.client.transport.TransportClientNodesService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.ImmutableMap;


/**
 * The Class InternalTransportClient.
 *
 * @author l.xue.nong
 */
public class InternalTransportClient extends AbstractClient implements InternalClient {

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The nodes service. */
	private final TransportClientNodesService nodesService;

	
	/** The admin client. */
	private final InternalTransportAdminClient adminClient;

	
	/** The actions. */
	private final ImmutableMap<Action, TransportActionNodeProxy> actions;

	
	/**
	 * Instantiates a new internal transport client.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param nodesService the nodes service
	 * @param adminClient the admin client
	 * @param actions the actions
	 */
	@Inject
	public InternalTransportClient(Settings settings, ThreadPool threadPool, TransportService transportService,
			TransportClientNodesService nodesService, InternalTransportAdminClient adminClient,
			Map<String, GenericAction> actions) {
		this.threadPool = threadPool;
		this.nodesService = nodesService;
		this.adminClient = adminClient;

		MapBuilder<Action, TransportActionNodeProxy> actionsBuilder = new MapBuilder<Action, TransportActionNodeProxy>();
		for (GenericAction action : actions.values()) {
			if (action instanceof Action) {
				actionsBuilder.put((Action) action, new TransportActionNodeProxy(action, transportService));
			}
		}
		this.actions = actionsBuilder.immutableMap();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#close()
	 */
	@Override
	public void close() {
		
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.internal.InternalClient#threadPool()
	 */
	@Override
	public ThreadPool threadPool() {
		return this.threadPool;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#admin()
	 */
	@Override
	public AdminClient admin() {
		return adminClient;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#execute(cn.com.summall.search.core.action.Action, cn.com.summall.search.core.action.ActionRequest)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> ActionFuture<Response> execute(
			final Action<Request, Response, RequestBuilder> action, final Request request) {
		final TransportActionNodeProxy<Request, Response> proxy = actions.get(action);
		return nodesService.execute(new TransportClientNodesService.NodeCallback<ActionFuture<Response>>() {
			@Override
			public ActionFuture<Response> doWithNode(DiscoveryNode node) throws RestartException {
				return proxy.execute(node, request);
			}
		});
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#execute(cn.com.summall.search.core.action.Action, cn.com.summall.search.core.action.ActionRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> void execute(
			final Action<Request, Response, RequestBuilder> action, final Request request,
			ActionListener<Response> listener) {
		final TransportActionNodeProxy<Request, Response> proxy = actions.get(action);
		nodesService.execute(new TransportClientNodesService.NodeListenerCallback<Response>() {
			@Override
			public void doWithNode(DiscoveryNode node, ActionListener<Response> listener) throws RestartException {
				proxy.execute(node, request, listener);
			}
		}, listener);
	}
}
