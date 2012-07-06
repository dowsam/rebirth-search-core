/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RefreshAction.java 2012-3-29 15:02:06 l.xue.nong$$
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
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public RefreshResponse newResponse() {
		return new RefreshResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
	 */
	@Override
	public RefreshRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new RefreshRequestBuilder(client);
	}
}
