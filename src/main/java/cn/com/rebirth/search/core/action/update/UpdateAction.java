/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core UpdateAction.java 2012-3-29 15:01:15 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.update;

import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.client.Client;


/**
 * The Class UpdateAction.
 *
 * @author l.xue.nong
 */
public class UpdateAction extends Action<UpdateRequest, UpdateResponse, UpdateRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final UpdateAction INSTANCE = new UpdateAction();

	
	/** The Constant NAME. */
	public static final String NAME = "update";

	
	/**
	 * Instantiates a new update action.
	 */
	private UpdateAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public UpdateResponse newResponse() {
		return new UpdateResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.Action#newRequestBuilder(cn.com.summall.search.core.client.Client)
	 */
	@Override
	public UpdateRequestBuilder newRequestBuilder(Client client) {
		return new UpdateRequestBuilder(client);
	}
}
