/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PutMappingAction.java 2012-3-29 15:02:06 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.mapping.put;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class PutMappingAction.
 *
 * @author l.xue.nong
 */
public class PutMappingAction extends IndicesAction<PutMappingRequest, PutMappingResponse, PutMappingRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final PutMappingAction INSTANCE = new PutMappingAction();

	
	/** The Constant NAME. */
	public static final String NAME = "indices/mapping/put";

	
	/**
	 * Instantiates a new put mapping action.
	 */
	private PutMappingAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public PutMappingResponse newResponse() {
		return new PutMappingResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
	 */
	@Override
	public PutMappingRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new PutMappingRequestBuilder(client);
	}
}
