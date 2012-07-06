/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core Action.java 2012-7-6 14:29:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

import cn.com.rebirth.search.core.client.Client;

/**
 * The Class Action.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @param <RequestBuilder> the generic type
 * @author l.xue.nong
 */
public abstract class Action<Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>>
		extends GenericAction<Request, Response> {

	/**
	 * Instantiates a new action.
	 *
	 * @param name the name
	 */
	protected Action(String name) {
		super(name);
	}

	/**
	 * New request builder.
	 *
	 * @param client the client
	 * @return the request builder
	 */
	public abstract RequestBuilder newRequestBuilder(Client client);
}
