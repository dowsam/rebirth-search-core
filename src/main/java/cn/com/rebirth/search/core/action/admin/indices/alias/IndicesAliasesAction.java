/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesAliasesAction.java 2012-7-6 14:30:01 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.alias;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class IndicesAliasesAction.
 *
 * @author l.xue.nong
 */
public class IndicesAliasesAction extends
		IndicesAction<IndicesAliasesRequest, IndicesAliasesResponse, IndicesAliasesRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final IndicesAliasesAction INSTANCE = new IndicesAliasesAction();

	/** The Constant NAME. */
	public static final String NAME = "indices/aliases";

	/**
	 * Instantiates a new indices aliases action.
	 */
	private IndicesAliasesAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public IndicesAliasesResponse newResponse() {
		return new IndicesAliasesResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.rebirth.search.core.client.IndicesAdminClient)
	 */
	@Override
	public IndicesAliasesRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new IndicesAliasesRequestBuilder(client);
	}
}
