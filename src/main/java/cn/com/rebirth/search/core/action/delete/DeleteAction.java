/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DeleteAction.java 2012-3-29 15:02:31 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.delete;

import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.client.Client;


/**
 * The Class DeleteAction.
 *
 * @author l.xue.nong
 */
public class DeleteAction extends Action<DeleteRequest, DeleteResponse, DeleteRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final DeleteAction INSTANCE = new DeleteAction();

	
	/** The Constant NAME. */
	public static final String NAME = "delete";

	
	/**
	 * Instantiates a new delete action.
	 */
	private DeleteAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public DeleteResponse newResponse() {
		return new DeleteResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.Action#newRequestBuilder(cn.com.summall.search.core.client.Client)
	 */
	@Override
	public DeleteRequestBuilder newRequestBuilder(Client client) {
		return new DeleteRequestBuilder(client);
	}
}
