/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GetAction.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.get;

import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.client.Client;

/**
 * The Class GetAction.
 *
 * @author l.xue.nong
 */
public class GetAction extends Action<GetRequest, GetResponse, GetRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final GetAction INSTANCE = new GetAction();

	/** The Constant NAME. */
	public static final String NAME = "get";

	/**
	 * Instantiates a new gets the action.
	 */
	private GetAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public GetResponse newResponse() {
		return new GetResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.Action#newRequestBuilder(cn.com.rebirth.search.core.client.Client)
	 */
	@Override
	public GetRequestBuilder newRequestBuilder(Client client) {
		return new GetRequestBuilder(client);
	}
}
