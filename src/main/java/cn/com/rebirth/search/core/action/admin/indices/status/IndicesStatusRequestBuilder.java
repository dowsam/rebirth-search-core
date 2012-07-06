/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesStatusRequestBuilder.java 2012-3-29 15:01:51 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.status;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class IndicesStatusRequestBuilder.
 *
 * @author l.xue.nong
 */
public class IndicesStatusRequestBuilder extends BaseIndicesRequestBuilder<IndicesStatusRequest, IndicesStatusResponse> {

	
	/**
	 * Instantiates a new indices status request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public IndicesStatusRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new IndicesStatusRequest());
	}

	
	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the indices status request builder
	 */
	public IndicesStatusRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	
	/**
	 * Sets the recovery.
	 *
	 * @param recovery the recovery
	 * @return the indices status request builder
	 */
	public IndicesStatusRequestBuilder setRecovery(boolean recovery) {
		request.recovery(recovery);
		return this;
	}

	
	/**
	 * Sets the snapshot.
	 *
	 * @param snapshot the snapshot
	 * @return the indices status request builder
	 */
	public IndicesStatusRequestBuilder setSnapshot(boolean snapshot) {
		request.snapshot(snapshot);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<IndicesStatusResponse> listener) {
		client.status(request, listener);
	}
}
