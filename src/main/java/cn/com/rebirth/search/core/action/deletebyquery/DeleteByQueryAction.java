/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DeleteByQueryAction.java 2012-3-29 15:01:30 l.xue.nong$$
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
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public DeleteByQueryResponse newResponse() {
		return new DeleteByQueryResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.Action#newRequestBuilder(cn.com.summall.search.core.client.Client)
	 */
	@Override
	public DeleteByQueryRequestBuilder newRequestBuilder(Client client) {
		return new DeleteByQueryRequestBuilder(client);
	}
}
