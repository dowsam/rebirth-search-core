/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core OptimizeAction.java 2012-7-6 14:29:00 l.xue.nong$$
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
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public OptimizeResponse newResponse() {
		return new OptimizeResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.rebirth.search.core.client.IndicesAdminClient)
	 */
	@Override
	public OptimizeRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new OptimizeRequestBuilder(client);
	}
}
