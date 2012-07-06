/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RefreshAction.java 2012-7-6 14:28:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.refresh;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class RefreshAction.
 *
 * @author l.xue.nong
 */
public class RefreshAction extends IndicesAction<RefreshRequest, RefreshResponse, RefreshRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final RefreshAction INSTANCE = new RefreshAction();

	/** The Constant NAME. */
	public static final String NAME = "indices/refresh";

	/**
	 * Instantiates a new refresh action.
	 */
	private RefreshAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public RefreshResponse newResponse() {
		return new RefreshResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.rebirth.search.core.client.IndicesAdminClient)
	 */
	@Override
	public RefreshRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new RefreshRequestBuilder(client);
	}
}
