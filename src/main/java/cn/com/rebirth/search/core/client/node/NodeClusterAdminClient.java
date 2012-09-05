/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeClusterAdminClient.java 2012-7-6 14:30:03 l.xue.nong$$
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
import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.client.internal.InternalClusterAdminClient;
import cn.com.rebirth.search.core.client.support.AbstractClusterAdminClient;

import com.google.common.collect.ImmutableMap;

/**
 * The Class NodeClusterAdminClient.
 *
 * @author l.xue.nong
 */
public class NodeClusterAdminClient extends AbstractClusterAdminClient implements InternalClusterAdminClient {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The actions. */
	private final ImmutableMap<ClusterAction, TransportAction> actions;

	/**
	 * Instantiates a new node cluster admin client.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param actions the actions
	 */
	@Inject
	public NodeClusterAdminClient(Settings settings, ThreadPool threadPool, Map<GenericAction, TransportAction> actions) {
		this.threadPool = threadPool;
		MapBuilder<ClusterAction, TransportAction> actionsBuilder = new MapBuilder<ClusterAction, TransportAction>();
		for (Map.Entry<GenericAction, TransportAction> entry : actions.entrySet()) {
			if (entry.getKey() instanceof ClusterAction) {
				actionsBuilder.put((ClusterAction) entry.getKey(), entry.getValue());
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
			ClusterAction<Request, Response, RequestBuilder> action, Request request) {
		TransportAction<Request, Response> transportAction = actions.get(action);
		return transportAction.execute(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.ClusterAdminClient#execute(cn.com.rebirth.search.core.action.admin.cluster.ClusterAction, cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> void execute(
			ClusterAction<Request, Response, RequestBuilder> action, Request request, ActionListener<Response> listener) {
		TransportAction<Request, Response> transportAction = actions.get(action);
		transportAction.execute(request, listener);
	}
}
