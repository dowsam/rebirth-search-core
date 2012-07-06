/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DeleteMappingAction.java 2012-3-29 15:01:48 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.mapping.delete;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class DeleteMappingAction.
 *
 * @author l.xue.nong
 */
public class DeleteMappingAction extends
		IndicesAction<DeleteMappingRequest, DeleteMappingResponse, DeleteMappingRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final DeleteMappingAction INSTANCE = new DeleteMappingAction();

	
	/** The Constant NAME. */
	public static final String NAME = "indices/mapping/delete";

	
	/**
	 * Instantiates a new delete mapping action.
	 */
	private DeleteMappingAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public DeleteMappingResponse newResponse() {
		return new DeleteMappingResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
	 */
	@Override
	public DeleteMappingRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new DeleteMappingRequestBuilder(client);
	}
}
