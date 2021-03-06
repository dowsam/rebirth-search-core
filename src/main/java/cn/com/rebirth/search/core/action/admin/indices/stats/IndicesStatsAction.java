/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesStatsAction.java 2012-7-6 14:30:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.stats;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class IndicesStatsAction.
 *
 * @author l.xue.nong
 */
public class IndicesStatsAction extends IndicesAction<IndicesStatsRequest, IndicesStats, IndicesStatsRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final IndicesStatsAction INSTANCE = new IndicesStatsAction();

	/** The Constant NAME. */
	public static final String NAME = "indices/stats";

	/**
	 * Instantiates a new indices stats action.
	 */
	private IndicesStatsAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public IndicesStats newResponse() {
		return new IndicesStats();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.rebirth.search.core.client.IndicesAdminClient)
	 */
	@Override
	public IndicesStatsRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new IndicesStatsRequestBuilder(client);
	}
}
