/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DeleteIndexTemplateAction.java 2012-3-29 15:02:37 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.template.delete;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class DeleteIndexTemplateAction.
 *
 * @author l.xue.nong
 */
public class DeleteIndexTemplateAction extends
		IndicesAction<DeleteIndexTemplateRequest, DeleteIndexTemplateResponse, DeleteIndexTemplateRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final DeleteIndexTemplateAction INSTANCE = new DeleteIndexTemplateAction();

	
	/** The Constant NAME. */
	public static final String NAME = "indices/template/delete";

	
	/**
	 * Instantiates a new delete index template action.
	 */
	private DeleteIndexTemplateAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public DeleteIndexTemplateResponse newResponse() {
		return new DeleteIndexTemplateResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
	 */
	@Override
	public DeleteIndexTemplateRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new DeleteIndexTemplateRequestBuilder(client);
	}
}
