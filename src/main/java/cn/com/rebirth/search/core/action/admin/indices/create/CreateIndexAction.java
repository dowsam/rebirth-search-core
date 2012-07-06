/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CreateIndexAction.java 2012-3-29 15:01:48 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.create;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class CreateIndexAction.
 *
 * @author l.xue.nong
 */
public class CreateIndexAction extends
		IndicesAction<CreateIndexRequest, CreateIndexResponse, CreateIndexRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final CreateIndexAction INSTANCE = new CreateIndexAction();

	
	/** The Constant NAME. */
	public static final String NAME = "indices/create";

	
	/**
	 * Instantiates a new creates the index action.
	 */
	private CreateIndexAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public CreateIndexResponse newResponse() {
		return new CreateIndexResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
	 */
	@Override
	public CreateIndexRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new CreateIndexRequestBuilder(client);
	}
}
