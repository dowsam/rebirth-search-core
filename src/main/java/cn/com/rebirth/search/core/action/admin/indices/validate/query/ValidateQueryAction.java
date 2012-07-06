/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ValidateQueryAction.java 2012-3-29 15:01:02 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.validate.query;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class ValidateQueryAction.
 *
 * @author l.xue.nong
 */
public class ValidateQueryAction extends
		IndicesAction<ValidateQueryRequest, ValidateQueryResponse, ValidateQueryRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final ValidateQueryAction INSTANCE = new ValidateQueryAction();

	
	/** The Constant NAME. */
	public static final String NAME = "indices/validate/query";

	
	/**
	 * Instantiates a new validate query action.
	 */
	private ValidateQueryAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public ValidateQueryResponse newResponse() {
		return new ValidateQueryResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
	 */
	@Override
	public ValidateQueryRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new ValidateQueryRequestBuilder(client);
	}
}
