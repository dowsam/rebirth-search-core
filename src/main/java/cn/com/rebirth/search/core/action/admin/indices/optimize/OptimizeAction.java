/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core OptimizeAction.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.optimize;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class OptimizeAction.
 *
 * @author l.xue.nong
 */
public class OptimizeAction extends IndicesAction<OptimizeRequest, OptimizeResponse, OptimizeRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final OptimizeAction INSTANCE = new OptimizeAction();

	
	/** The Constant NAME. */
	public static final String NAME = "indices/optimize";

	
	/**
	 * Instantiates a new optimize action.
	 */
	private OptimizeAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public OptimizeResponse newResponse() {
		return new OptimizeResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
	 */
	@Override
	public OptimizeRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new OptimizeRequestBuilder(client);
	}
}
