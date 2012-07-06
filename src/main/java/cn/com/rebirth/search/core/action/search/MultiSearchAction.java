/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MultiSearchAction.java 2012-3-29 15:02:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.search;

import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.client.Client;


/**
 * The Class MultiSearchAction.
 *
 * @author l.xue.nong
 */
public class MultiSearchAction extends Action<MultiSearchRequest, MultiSearchResponse, MultiSearchRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final MultiSearchAction INSTANCE = new MultiSearchAction();

	
	/** The Constant NAME. */
	public static final String NAME = "msearch";

	
	/**
	 * Instantiates a new multi search action.
	 */
	private MultiSearchAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public MultiSearchResponse newResponse() {
		return new MultiSearchResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.Action#newRequestBuilder(cn.com.summall.search.core.client.Client)
	 */
	@Override
	public MultiSearchRequestBuilder newRequestBuilder(Client client) {
		return new MultiSearchRequestBuilder(client);
	}
}
