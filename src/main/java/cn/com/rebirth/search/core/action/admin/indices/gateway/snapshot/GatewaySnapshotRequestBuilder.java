/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GatewaySnapshotRequestBuilder.java 2012-3-29 15:02:52 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class GatewaySnapshotRequestBuilder.
 *
 * @author l.xue.nong
 */
public class GatewaySnapshotRequestBuilder extends
		BaseIndicesRequestBuilder<GatewaySnapshotRequest, GatewaySnapshotResponse> {

	
	/**
	 * Instantiates a new gateway snapshot request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public GatewaySnapshotRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new GatewaySnapshotRequest());
	}

	
	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the gateway snapshot request builder
	 */
	public GatewaySnapshotRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<GatewaySnapshotResponse> listener) {
		client.gatewaySnapshot(request, listener);
	}
}
