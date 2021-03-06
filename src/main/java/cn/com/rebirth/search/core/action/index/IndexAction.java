/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexAction.java 2012-7-6 14:29:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.index;

import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.client.Client;

/**
 * The Class IndexAction.
 *
 * @author l.xue.nong
 */
public class IndexAction extends Action<IndexRequest, IndexResponse, IndexRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final IndexAction INSTANCE = new IndexAction();

	/** The Constant NAME. */
	public static final String NAME = "index";

	/**
	 * Instantiates a new index action.
	 */
	private IndexAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public IndexResponse newResponse() {
		return new IndexResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.Action#newRequestBuilder(cn.com.rebirth.search.core.client.Client)
	 */
	@Override
	public IndexRequestBuilder newRequestBuilder(Client client) {
		return new IndexRequestBuilder(client);
	}
}
