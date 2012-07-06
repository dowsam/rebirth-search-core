/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ActionRequestBuilder.java 2012-7-6 14:30:29 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

/**
 * The Interface ActionRequestBuilder.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @author l.xue.nong
 */
public interface ActionRequestBuilder<Request extends ActionRequest, Response extends ActionResponse> {

	/**
	 * Request.
	 *
	 * @return the request
	 */
	Request request();

	/**
	 * Execute.
	 *
	 * @return the listenable action future
	 */
	ListenableActionFuture<Response> execute();

	/**
	 * Execute.
	 *
	 * @param listener the listener
	 */
	void execute(ActionListener<Response> listener);
}
