/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesExistsRequestBuilder.java 2012-7-6 14:29:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.exists;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class IndicesExistsRequestBuilder.
 *
 * @author l.xue.nong
 */
public class IndicesExistsRequestBuilder extends BaseIndicesRequestBuilder<IndicesExistsRequest, IndicesExistsResponse> {

	/**
	 * Instantiates a new indices exists request builder.
	 *
	 * @param indicesClient the indices client
	 * @param indices the indices
	 */
	public IndicesExistsRequestBuilder(IndicesAdminClient indicesClient, String... indices) {
		super(indicesClient, new IndicesExistsRequest(indices));
	}

	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the indices exists request builder
	 */
	public IndicesExistsRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<IndicesExistsResponse> listener) {
		client.exists(request, listener);
	}
}
