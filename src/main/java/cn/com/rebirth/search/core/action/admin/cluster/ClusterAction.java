/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterAction.java 2012-7-6 14:30:26 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster;

import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.GenericAction;
import cn.com.rebirth.search.core.client.ClusterAdminClient;

/**
 * The Class ClusterAction.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @param <RequestBuilder> the generic type
 * @author l.xue.nong
 */
public abstract class ClusterAction<Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>>
		extends GenericAction<Request, Response> {

	/**
	 * Instantiates a new cluster action.
	 *
	 * @param name the name
	 */
	protected ClusterAction(String name) {
		super(name);
	}

	/**
	 * New request builder.
	 *
	 * @param client the client
	 * @return the request builder
	 */
	public abstract RequestBuilder newRequestBuilder(ClusterAdminClient client);
}
