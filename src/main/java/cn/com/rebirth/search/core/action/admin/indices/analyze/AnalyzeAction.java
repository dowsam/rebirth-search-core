/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AnalyzeAction.java 2012-3-29 15:02:28 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.analyze;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class AnalyzeAction.
 *
 * @author l.xue.nong
 */
public class AnalyzeAction extends IndicesAction<AnalyzeRequest, AnalyzeResponse, AnalyzeRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final AnalyzeAction INSTANCE = new AnalyzeAction();

	
	/** The Constant NAME. */
	public static final String NAME = "indices/analyze";

	
	/**
	 * Instantiates a new analyze action.
	 */
	private AnalyzeAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public AnalyzeResponse newResponse() {
		return new AnalyzeResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
	 */
	@Override
	public AnalyzeRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new AnalyzeRequestBuilder(client);
	}
}
