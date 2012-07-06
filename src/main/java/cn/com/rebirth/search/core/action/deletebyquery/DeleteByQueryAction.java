/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DeleteByQueryAction.java 2012-7-6 14:30:07 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.deletebyquery;

import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.client.Client;

/**
 * The Class DeleteByQueryAction.
 *
 * @author l.xue.nong
 */
public class DeleteByQueryAction extends
		Action<DeleteByQueryRequest, DeleteByQueryResponse, DeleteByQueryRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final DeleteByQueryAction INSTANCE = new DeleteByQueryAction();

	/** The Constant NAME. */
	public static final String NAME = "deleteByQuery";

	/**
	 * Instantiates a new delete by query action.
	 */
	private DeleteByQueryAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public DeleteByQueryResponse newResponse() {
		return new DeleteByQueryResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.Action#newRequestBuilder(cn.com.rebirth.search.core.client.Client)
	 */
	@Override
	public DeleteByQueryRequestBuilder newRequestBuilder(Client client) {
		return new DeleteByQueryRequestBuilder(client);
	}
}
