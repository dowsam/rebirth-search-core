/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PutMappingAction.java 2012-7-6 14:28:43 l.xue.nong$$
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
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public PutMappingResponse newResponse() {
		return new PutMappingResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.rebirth.search.core.client.IndicesAdminClient)
	 */
	@Override
	public PutMappingRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new PutMappingRequestBuilder(client);
	}
}
