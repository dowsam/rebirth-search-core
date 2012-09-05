/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeIndicesAdminClient.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client.node;

import java.util.Map;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.GenericAction;
import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;
import cn.com.rebirth.search.core.client.support.AbstractIndicesAdminClient;

import com.google.common.collect.ImmutableMap;

/**
 * The Class NodeIndicesAdminClient.
 *
 * @author l.xue.nong
 */
public class NodeIndicesAdminClient extends AbstractIndicesAdminClient implements IndicesAdminClient {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The actions. */
	private final ImmutableMap<IndicesAction, TransportAction> actions;

	/**
	 * Instantiates a new node indices admin client.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param actions the actions
	 */
	@Inject
	public NodeIndicesAdminClient(Settings settings, ThreadPool threadPool, Map<GenericAction, TransportAction> actions) {
		this.threadPool = threadPool;
		MapBuilder<IndicesAction, TransportAction> actionsBuilder = new MapBuilder<IndicesAction, TransportAction>();
		for (Map.Entry<GenericAction, TransportAction> entry : actions.entrySet()) {
			if (entry.getKey() instanceof IndicesAction) {
				actionsBuilder.put((IndicesAction) entry.getKey(), entry.getValue());
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
			IndicesAction<Request, Response, RequestBuilder> action, Request request) {
		TransportAction<Request, Response> transportAction = actions.get(action);
		return transportAction.execute(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.IndicesAdminClient#execute(cn.com.rebirth.search.core.action.admin.indices.IndicesAction, cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> void execute(
			IndicesAction<Request, Response, RequestBuilder> action, Request request, ActionListener<Response> listener) {
		TransportAction<Request, Response> transportAction = actions.get(action);
		transportAction.execute(request, listener);
	}
}
