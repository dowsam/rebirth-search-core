/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchScrollAction.java 2012-3-29 15:02:06 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.search;

import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.client.Client;


/**
 * The Class SearchScrollAction.
 *
 * @author l.xue.nong
 */
public class SearchScrollAction extends Action<SearchScrollRequest, SearchResponse, SearchScrollRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final SearchScrollAction INSTANCE = new SearchScrollAction();

	
	/** The Constant NAME. */
	public static final String NAME = "search/scroll";

	
	/**
	 * Instantiates a new search scroll action.
	 */
	private SearchScrollAction() {
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
	public SearchScrollRequestBuilder newRequestBuilder(Client client) {
		return new SearchScrollRequestBuilder(client);
	}
}
