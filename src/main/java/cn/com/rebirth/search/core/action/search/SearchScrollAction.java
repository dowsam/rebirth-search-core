/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchScrollAction.java 2012-7-6 14:28:56 l.xue.nong$$
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
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public SearchResponse newResponse() {
		return new SearchResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.Action#newRequestBuilder(cn.com.rebirth.search.core.client.Client)
	 */
	@Override
	public SearchScrollRequestBuilder newRequestBuilder(Client client) {
		return new SearchScrollRequestBuilder(client);
	}
}
