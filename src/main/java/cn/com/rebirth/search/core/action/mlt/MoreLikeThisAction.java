/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MoreLikeThisAction.java 2012-7-6 14:29:31 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.mlt;

import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.action.search.SearchResponse;
import cn.com.rebirth.search.core.client.Client;

/**
 * The Class MoreLikeThisAction.
 *
 * @author l.xue.nong
 */
public class MoreLikeThisAction extends Action<MoreLikeThisRequest, SearchResponse, MoreLikeThisRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final MoreLikeThisAction INSTANCE = new MoreLikeThisAction();

	/** The Constant NAME. */
	public static final String NAME = "mlt";

	/**
	 * Instantiates a new more like this action.
	 */
	private MoreLikeThisAction() {
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
	public MoreLikeThisRequestBuilder newRequestBuilder(Client client) {
		return new MoreLikeThisRequestBuilder(client);
	}
}
