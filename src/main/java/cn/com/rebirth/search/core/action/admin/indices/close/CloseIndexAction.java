/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CloseIndexAction.java 2012-7-6 14:29:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.close;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class CloseIndexAction.
 *
 * @author l.xue.nong
 */
public class CloseIndexAction extends IndicesAction<CloseIndexRequest, CloseIndexResponse, CloseIndexRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final CloseIndexAction INSTANCE = new CloseIndexAction();

	/** The Constant NAME. */
	public static final String NAME = "indices/close";

	/**
	 * Instantiates a new close index action.
	 */
	private CloseIndexAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public CloseIndexResponse newResponse() {
		return new CloseIndexResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.rebirth.search.core.client.IndicesAdminClient)
	 */
	@Override
	public CloseIndexRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new CloseIndexRequestBuilder(client);
	}
}
