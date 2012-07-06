/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BaseIndicesRequestBuilder.java 2012-7-6 14:30:26 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.support;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.ListenableActionFuture;
import cn.com.rebirth.search.core.action.support.PlainListenableActionFuture;
import cn.com.rebirth.search.core.client.IndicesAdminClient;
import cn.com.rebirth.search.core.client.internal.InternalIndicesAdminClient;

/**
 * The Class BaseIndicesRequestBuilder.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @author l.xue.nong
 */
public abstract class BaseIndicesRequestBuilder<Request extends ActionRequest, Response extends ActionResponse>
		implements ActionRequestBuilder<Request, Response> {

	/** The client. */
	protected final InternalIndicesAdminClient client;

	/** The request. */
	protected final Request request;

	/**
	 * Instantiates a new base indices request builder.
	 *
	 * @param client the client
	 * @param request the request
	 */
	protected BaseIndicesRequestBuilder(IndicesAdminClient client, Request request) {
		this.client = (InternalIndicesAdminClient) client;
		this.request = request;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequestBuilder#request()
	 */
	@Override
	public Request request() {
		return request;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequestBuilder#execute()
	 */
	@Override
	public ListenableActionFuture<Response> execute() {
		PlainListenableActionFuture<Response> future = new PlainListenableActionFuture<Response>(
				request.listenerThreaded(), client.threadPool());
		execute(future);
		return future;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequestBuilder#execute(cn.com.rebirth.search.core.action.ActionListener)
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