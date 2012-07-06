/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesSegmentsAction.java 2012-7-6 14:29:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.segments;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class IndicesSegmentsAction.
 *
 * @author l.xue.nong
 */
public class IndicesSegmentsAction extends
		IndicesAction<IndicesSegmentsRequest, IndicesSegmentResponse, IndicesSegmentsRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final IndicesSegmentsAction INSTANCE = new IndicesSegmentsAction();

	/** The Constant NAME. */
	public static final String NAME = "indices/segments";

	/**
	 * Instantiates a new indices segments action.
	 */
	private IndicesSegmentsAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public IndicesSegmentResponse newResponse() {
		return new IndicesSegmentResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.rebirth.search.core.client.IndicesAdminClient)
	 */
	@Override
	public IndicesSegmentsRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new IndicesSegmentsRequestBuilder(client);
	}
}
