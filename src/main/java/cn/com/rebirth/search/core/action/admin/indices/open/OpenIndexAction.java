/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core OpenIndexAction.java 2012-7-6 14:29:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.open;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class OpenIndexAction.
 *
 * @author l.xue.nong
 */
public class OpenIndexAction extends IndicesAction<OpenIndexRequest, OpenIndexResponse, OpenIndexRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final OpenIndexAction INSTANCE = new OpenIndexAction();

	/** The Constant NAME. */
	public static final String NAME = "indices/open";

	/**
	 * Instantiates a new open index action.
	 */
	private OpenIndexAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public OpenIndexResponse newResponse() {
		return new OpenIndexResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.rebirth.search.core.client.IndicesAdminClient)
	 */
	@Override
	public OpenIndexRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new OpenIndexRequestBuilder(client);
	}
}
