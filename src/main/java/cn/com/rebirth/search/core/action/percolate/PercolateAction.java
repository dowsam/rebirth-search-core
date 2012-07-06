/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PercolateAction.java 2012-7-6 14:30:39 l.xue.nong$$
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
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public PercolateResponse newResponse() {
		return new PercolateResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.Action#newRequestBuilder(cn.com.rebirth.search.core.client.Client)
	 */
	@Override
	public PercolateRequestBuilder newRequestBuilder(Client client) {
		return new PercolateRequestBuilder(client);
	}
}
