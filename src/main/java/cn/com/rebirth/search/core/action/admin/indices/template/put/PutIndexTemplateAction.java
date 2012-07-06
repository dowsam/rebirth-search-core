/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PutIndexTemplateAction.java 2012-3-29 15:02:28 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.template.put;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class PutIndexTemplateAction.
 *
 * @author l.xue.nong
 */
public class PutIndexTemplateAction extends
		IndicesAction<PutIndexTemplateRequest, PutIndexTemplateResponse, PutIndexTemplateRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final PutIndexTemplateAction INSTANCE = new PutIndexTemplateAction();
	
	
	/** The Constant NAME. */
	public static final String NAME = "indices/template/put";

	
	/**
	 * Instantiates a new put index template action.
	 */
	private PutIndexTemplateAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public PutIndexTemplateResponse newResponse() {
		return new PutIndexTemplateResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
	 */
	@Override
	public PutIndexTemplateRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new PutIndexTemplateRequestBuilder(client);
	}
}
