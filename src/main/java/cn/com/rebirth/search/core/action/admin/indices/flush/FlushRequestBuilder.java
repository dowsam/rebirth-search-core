/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FlushRequestBuilder.java 2012-3-29 15:01:29 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.flush;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class FlushRequestBuilder.
 *
 * @author l.xue.nong
 */
public class FlushRequestBuilder extends BaseIndicesRequestBuilder<FlushRequest, FlushResponse> {

	
	/**
	 * Instantiates a new flush request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public FlushRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new FlushRequest());
	}

	
	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the flush request builder
	 */
	public FlushRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	
	/**
	 * Sets the refresh.
	 *
	 * @param refresh the refresh
	 * @return the flush request builder
	 */
	public FlushRequestBuilder setRefresh(boolean refresh) {
		request.refresh(refresh);
		return this;
	}

	
	/**
	 * Sets the full.
	 *
	 * @param full the full
	 * @return the flush request builder
	 */
	public FlushRequestBuilder setFull(boolean full) {
		request.full(full);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<FlushResponse> listener) {
		client.flush(request, listener);
	}
}
