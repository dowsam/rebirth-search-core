/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GatewaySnapshotRequest.java 2012-3-29 15:02:17 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot;

import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest;


/**
 * The Class GatewaySnapshotRequest.
 *
 * @author l.xue.nong
 */
public class GatewaySnapshotRequest extends BroadcastOperationRequest {

	
	/**
	 * Instantiates a new gateway snapshot request.
	 */
	GatewaySnapshotRequest() {

	}

	
	/**
	 * Instantiates a new gateway snapshot request.
	 *
	 * @param indices the indices
	 */
	public GatewaySnapshotRequest(String... indices) {
		this.indices = indices;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#listenerThreaded(boolean)
	 */
	@Override
	public GatewaySnapshotRequest listenerThreaded(boolean threadedListener) {
		super.listenerThreaded(threadedListener);
		return this;
	}
}