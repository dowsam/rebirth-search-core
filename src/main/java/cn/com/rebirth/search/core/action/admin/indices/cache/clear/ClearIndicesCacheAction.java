/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClearIndicesCacheAction.java 2012-7-6 14:29:02 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.cache.clear;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class ClearIndicesCacheAction.
 *
 * @author l.xue.nong
 */
public class ClearIndicesCacheAction extends
		IndicesAction<ClearIndicesCacheRequest, ClearIndicesCacheResponse, ClearIndicesCacheRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final ClearIndicesCacheAction INSTANCE = new ClearIndicesCacheAction();

	/** The Constant NAME. */
	public static final String NAME = "indices/cache/clear";

	/**
	 * Instantiates a new clear indices cache action.
	 */
	private ClearIndicesCacheAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public ClearIndicesCacheResponse newResponse() {
		return new ClearIndicesCacheResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.rebirth.search.core.client.IndicesAdminClient)
	 */
	@Override
	public ClearIndicesCacheRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new ClearIndicesCacheRequestBuilder(client);
	}
}
