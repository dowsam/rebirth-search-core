/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BulkAction.java 2012-7-6 14:29:38 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.bulk;

import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.transport.TransportRequestOptions;

/**
 * The Class BulkAction.
 *
 * @author l.xue.nong
 */
public class BulkAction extends Action<BulkRequest, BulkResponse, BulkRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final BulkAction INSTANCE = new BulkAction();

	/** The Constant NAME. */
	public static final String NAME = "bulk";

	/**
	 * Instantiates a new bulk action.
	 */
	private BulkAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public BulkResponse newResponse() {
		return new BulkResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.Action#newRequestBuilder(cn.com.rebirth.search.core.client.Client)
	 */
	@Override
	public BulkRequestBuilder newRequestBuilder(Client client) {
		return new BulkRequestBuilder(client);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#options()
	 */
	@Override
	public TransportRequestOptions options() {
		return TransportRequestOptions.options().withLowType().withCompress(true);
	}
}
