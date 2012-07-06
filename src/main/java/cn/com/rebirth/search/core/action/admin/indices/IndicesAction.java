/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesAction.java 2012-3-29 15:01:30 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices;

import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.GenericAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class IndicesAction.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @param <RequestBuilder> the generic type
 * @author l.xue.nong
 */
public abstract class IndicesAction<Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>>
		extends GenericAction<Request, Response> {

	
	/**
	 * Instantiates a new indices action.
	 *
	 * @param name the name
	 */
	protected IndicesAction(String name) {
		super(name);
	}

	
	/**
	 * New request builder.
	 *
	 * @param client the client
	 * @return the request builder
	 */
	public abstract RequestBuilder newRequestBuilder(IndicesAdminClient client);
}
