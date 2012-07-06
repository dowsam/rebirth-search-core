/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesStatusAction.java 2012-3-29 15:00:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.status;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class IndicesStatusAction.
 *
 * @author l.xue.nong
 */
public class IndicesStatusAction extends
		IndicesAction<IndicesStatusRequest, IndicesStatusResponse, IndicesStatusRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final IndicesStatusAction INSTANCE = new IndicesStatusAction();

	
	/** The Constant NAME. */
	public static final String NAME = "indices/status";

	
	/**
	 * Instantiates a new indices status action.
	 */
	private IndicesStatusAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public IndicesStatusResponse newResponse() {
		return new IndicesStatusResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
	 */
	@Override
	public IndicesStatusRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new IndicesStatusRequestBuilder(client);
	}
}
