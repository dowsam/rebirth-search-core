/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BaseRequestBuilder.java 2012-3-29 15:02:42 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.ListenableActionFuture;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.client.internal.InternalClient;


/**
 * The Class BaseRequestBuilder.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @author l.xue.nong
 */
public abstract class BaseRequestBuilder<Request extends ActionRequest, Response extends ActionResponse> implements
		ActionRequestBuilder<Request, Response> {

	
	/** The client. */
	protected final InternalClient client;

	
	/** The request. */
	protected final Request request;

	
	/**
	 * Instantiates a new base request builder.
	 *
	 * @param client the client
	 * @param request the request
	 */
	protected BaseRequestBuilder(Client client, Request request) {
		this.client = (InternalClient) client;
		this.request = request;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequestBuilder#request()
	 */
	public Request request() {
		return this.request;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequestBuilder#execute()
	 */
	@Override
	public ListenableActionFuture<Response> execute() {
		PlainListenableActionFuture<Response> future = new PlainListenableActionFuture<Response>(
				request.listenerThreaded(), client.threadPool());
		execute(future);
		return future;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequestBuilder#execute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void execute(ActionListener<Response> listener) {
		doExecute(listener);
	}

	
	/**
	 * Do execute.
	 *
	 * @param listener the listener
	 */
	protected abstract void doExecute(ActionListener<Response> listener);
}
