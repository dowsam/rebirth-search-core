/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchAction.java 2012-3-29 15:01:05 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.search;

import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.client.Client;


/**
 * The Class SearchAction.
 *
 * @author l.xue.nong
 */
public class SearchAction extends Action<SearchRequest, SearchResponse, SearchRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final SearchAction INSTANCE = new SearchAction();

	
	/** The Constant NAME. */
	public static final String NAME = "search";

	
	/**
	 * Instantiates a new search action.
	 */
	private SearchAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public SearchResponse newResponse() {
		return new SearchResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.Action#newRequestBuilder(cn.com.summall.search.core.client.Client)
	 */
	@Override
	public SearchRequestBuilder newRequestBuilder(Client client) {
		return new SearchRequestBuilder(client);
	}
}
