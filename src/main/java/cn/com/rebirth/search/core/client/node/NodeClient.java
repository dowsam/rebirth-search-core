/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodeClient.java 2012-3-29 15:02:45 l.xue.nong$$
 */


package cn.com.rebirth.search.core.client.node;

import java.util.Map;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.GenericAction;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.client.AdminClient;
import cn.com.rebirth.search.core.client.internal.InternalClient;
import cn.com.rebirth.search.core.client.support.AbstractClient;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

import com.google.common.collect.ImmutableMap;


/**
 * The Class NodeClient.
 *
 * @author l.xue.nong
 */
public class NodeClient extends AbstractClient implements InternalClient {

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The admin. */
	private final NodeAdminClient admin;

	
	/** The actions. */
	private final ImmutableMap<Action, TransportAction> actions;

	
	/**
	 * Instantiates a new node client.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param admin the admin
	 * @param actions the actions
	 */
	@Inject
	public NodeClient(Settings settings, ThreadPool threadPool, NodeAdminClient admin,
			Map<GenericAction, TransportAction> actions) {
		this.threadPool = threadPool;
		this.admin = admin;
		MapBuilder<Action, TransportAction> actionsBuilder = new MapBuilder<Action, TransportAction>();
		for (Map.Entry<GenericAction, TransportAction> entry : actions.entrySet()) {
			if (entry.getKey() instanceof Action) {
				actionsBuilder.put((Action) entry.getKey(), entry.getValue());
			}
		}
		this.actions = actionsBuilder.immutableMap();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.internal.InternalClient#threadPool()
	 */
	@Override
	public ThreadPool threadPool() {
		return this.threadPool;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#close()
	 */
	@Override
	public void close() {
		
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#admin()
	 */
	@Override
	public AdminClient admin() {
		return this.admin;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#execute(cn.com.summall.search.core.action.Action, cn.com.summall.search.core.action.ActionRequest)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> ActionFuture<Response> execute(
			Action<Request, Response, RequestBuilder> action, Request request) {
		TransportAction<Request, Response> transportAction = actions.get(action);
		return transportAction.execute(request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#execute(cn.com.summall.search.core.action.Action, cn.com.summall.search.core.action.ActionRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> void execute(
			Action<Request, Response, RequestBuilder> action, Request request, ActionListener<Response> listener) {
		TransportAction<Request, Response> transportAction = actions.get(action);
		transportAction.execute(request, listener);
	}
}
