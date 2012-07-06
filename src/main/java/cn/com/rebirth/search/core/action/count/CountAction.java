/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CountAction.java 2012-7-6 14:28:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.count;

import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.client.Client;

/**
 * The Class CountAction.
 *
 * @author l.xue.nong
 */
public class CountAction extends Action<CountRequest, CountResponse, CountRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final CountAction INSTANCE = new CountAction();

	/** The Constant NAME. */
	public static final String NAME = "count";

	/**
	 * Instantiates a new count action.
	 */
	private CountAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public CountResponse newResponse() {
		return new CountResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.Action#newRequestBuilder(cn.com.rebirth.search.core.client.Client)
	 */
	@Override
	public CountRequestBuilder newRequestBuilder(Client client) {
		return new CountRequestBuilder(client);
	}
}
