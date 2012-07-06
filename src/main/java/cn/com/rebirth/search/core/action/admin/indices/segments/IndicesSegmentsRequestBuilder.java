/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesSegmentsRequestBuilder.java 2012-7-6 14:29:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.segments;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class IndicesSegmentsRequestBuilder.
 *
 * @author l.xue.nong
 */
public class IndicesSegmentsRequestBuilder extends
		BaseIndicesRequestBuilder<IndicesSegmentsRequest, IndicesSegmentResponse> {

	/**
	 * Instantiates a new indices segments request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public IndicesSegmentsRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new IndicesSegmentsRequest());
	}

	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the indices segments request builder
	 */
	public IndicesSegmentsRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<IndicesSegmentResponse> listener) {
		client.segments(request, listener);
	}
}
