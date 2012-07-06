/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MultiGetAction.java 2012-7-6 14:30:26 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.get;

import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.client.Client;

/**
 * The Class MultiGetAction.
 *
 * @author l.xue.nong
 */
public class MultiGetAction extends Action<MultiGetRequest, MultiGetResponse, MultiGetRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final MultiGetAction INSTANCE = new MultiGetAction();

	/** The Constant NAME. */
	public static final String NAME = "mget";

	/**
	 * Instantiates a new multi get action.
	 */
	private MultiGetAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public MultiGetResponse newResponse() {
		return new MultiGetResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.Action#newRequestBuilder(cn.com.rebirth.search.core.client.Client)
	 */
	@Override
	public MultiGetRequestBuilder newRequestBuilder(Client client) {
		return new MultiGetRequestBuilder(client);
	}
}
