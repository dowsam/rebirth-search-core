/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PercolateAction.java 2012-3-29 15:01:30 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.percolate;

import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.client.Client;


/**
 * The Class PercolateAction.
 *
 * @author l.xue.nong
 */
public class PercolateAction extends Action<PercolateRequest, PercolateResponse, PercolateRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final PercolateAction INSTANCE = new PercolateAction();

	
	/** The Constant NAME. */
	public static final String NAME = "percolate";

	
	/**
	 * Instantiates a new percolate action.
	 */
	private PercolateAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public PercolateResponse newResponse() {
		return new PercolateResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.Action#newRequestBuilder(cn.com.summall.search.core.client.Client)
	 */
	@Override
	public PercolateRequestBuilder newRequestBuilder(Client client) {
		return new PercolateRequestBuilder(client);
	}
}
