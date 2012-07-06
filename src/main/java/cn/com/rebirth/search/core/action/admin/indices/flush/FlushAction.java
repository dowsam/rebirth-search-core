/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FlushAction.java 2012-3-29 15:02:38 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.flush;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class FlushAction.
 *
 * @author l.xue.nong
 */
public class FlushAction extends IndicesAction<FlushRequest, FlushResponse, FlushRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final FlushAction INSTANCE = new FlushAction();

	
	/** The Constant NAME. */
	public static final String NAME = "indices/flush";

	
	/**
	 * Instantiates a new flush action.
	 */
	private FlushAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public FlushResponse newResponse() {
		return new FlushResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
	 */
	@Override
	public FlushRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new FlushRequestBuilder(client);
	}
}
