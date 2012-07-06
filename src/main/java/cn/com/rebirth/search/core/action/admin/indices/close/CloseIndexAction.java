/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CloseIndexAction.java 2012-3-29 15:01:56 l.xue.nong$$
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
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public CloseIndexResponse newResponse() {
		return new CloseIndexResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
	 */
	@Override
	public CloseIndexRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new CloseIndexRequestBuilder(client);
	}
}
