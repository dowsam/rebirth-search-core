/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DeleteIndexAction.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.delete;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class DeleteIndexAction.
 *
 * @author l.xue.nong
 */
public class DeleteIndexAction extends
		IndicesAction<DeleteIndexRequest, DeleteIndexResponse, DeleteIndexRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final DeleteIndexAction INSTANCE = new DeleteIndexAction();

	/** The Constant NAME. */
	public static final String NAME = "indices/delete";

	/**
	 * Instantiates a new delete index action.
	 */
	private DeleteIndexAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public DeleteIndexResponse newResponse() {
		return new DeleteIndexResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.rebirth.search.core.client.IndicesAdminClient)
	 */
	@Override
	public DeleteIndexRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new DeleteIndexRequestBuilder(client);
	}
}
