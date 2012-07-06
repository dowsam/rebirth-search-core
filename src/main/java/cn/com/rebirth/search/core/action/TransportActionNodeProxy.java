/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportActionNodeProxy.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.support.PlainActionFuture;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportResponseHandler;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportService;


/**
 * The Class TransportActionNodeProxy.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @author l.xue.nong
 */
public class TransportActionNodeProxy<Request extends ActionRequest, Response extends ActionResponse> {

	
	/** The transport service. */
	protected final TransportService transportService;

	
	/** The action. */
	private final GenericAction<Request, Response> action;

	
	/**
	 * Instantiates a new transport action node proxy.
	 *
	 * @param action the action
	 * @param transportService the transport service
	 */
	@Inject
	public TransportActionNodeProxy(GenericAction<Request, Response> action, TransportService transportService) {
		this.action = action;
		this.transportService = transportService;
	}

	
	/**
	 * Execute.
	 *
	 * @param node the node
	 * @param request the request
	 * @return the action future
	 * @throws SumMallSearchException the sum mall search exception
	 */
	public ActionFuture<Response> execute(DiscoveryNode node, Request request) throws RestartException {
		PlainActionFuture<Response> future = PlainActionFuture.newFuture();
		request.listenerThreaded(false);
		execute(node, request, future);
		return future;
	}

	
	/**
	 * Execute.
	 *
	 * @param node the node
	 * @param request the request
	 * @param listener the listener
	 */
	public void execute(DiscoveryNode node, final Request request, final ActionListener<Response> listener) {
		transportService.sendRequest(node, action.name(), request, action.options(),
				new BaseTransportResponseHandler<Response>() {
					@Override
					public Response newInstance() {
						return action.newResponse();
					}

					@Override
					public String executor() {
						if (request.listenerThreaded()) {
							return ThreadPool.Names.GENERIC;
						}
						return ThreadPool.Names.SAME;
					}

					@Override
					public void handleResponse(Response response) {
						listener.onResponse(response);
					}

					@Override
					public void handleException(TransportException exp) {
						listener.onFailure(exp);
					}
				});
	}

}
