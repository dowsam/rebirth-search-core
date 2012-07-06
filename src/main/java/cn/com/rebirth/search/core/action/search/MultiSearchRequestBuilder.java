/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MultiSearchRequestBuilder.java 2012-3-29 15:02:40 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.search;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.support.BaseRequestBuilder;
import cn.com.rebirth.search.core.client.Client;


/**
 * The Class MultiSearchRequestBuilder.
 *
 * @author l.xue.nong
 */
public class MultiSearchRequestBuilder extends BaseRequestBuilder<MultiSearchRequest, MultiSearchResponse> {

	
	/**
	 * Instantiates a new multi search request builder.
	 *
	 * @param client the client
	 */
	public MultiSearchRequestBuilder(Client client) {
		super(client, new MultiSearchRequest());
	}

	
	/**
	 * Adds the.
	 *
	 * @param request the request
	 * @return the multi search request builder
	 */
	public MultiSearchRequestBuilder add(SearchRequest request) {
		super.request.add(request);
		return this;
	}

	
	/**
	 * Adds the.
	 *
	 * @param request the request
	 * @return the multi search request builder
	 */
	public MultiSearchRequestBuilder add(SearchRequestBuilder request) {
		super.request.add(request);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.BaseRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<MultiSearchResponse> listener) {
		client.multiSearch(request, listener);
	}
}
