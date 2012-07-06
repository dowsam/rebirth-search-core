/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RefreshRequestBuilder.java 2012-7-6 14:29:53 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.refresh;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class RefreshRequestBuilder.
 *
 * @author l.xue.nong
 */
public class RefreshRequestBuilder extends BaseIndicesRequestBuilder<RefreshRequest, RefreshResponse> {

	/**
	 * Instantiates a new refresh request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public RefreshRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new RefreshRequest());
	}

	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the refresh request builder
	 */
	public RefreshRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	/**
	 * Sets the wait for operations.
	 *
	 * @param waitForOperations the wait for operations
	 * @return the refresh request builder
	 */
	public RefreshRequestBuilder setWaitForOperations(boolean waitForOperations) {
		request.waitForOperations(waitForOperations);
		return this;
	}

	/**
	 * Sets the listener threaded.
	 *
	 * @param threadedListener the threaded listener
	 * @return the refresh request builder
	 */
	public RefreshRequestBuilder setListenerThreaded(boolean threadedListener) {
		request.listenerThreaded(threadedListener);
		return this;
	}

	/**
	 * Sets the operation threading.
	 *
	 * @param operationThreading the operation threading
	 * @return the refresh request builder
	 */
	public RefreshRequestBuilder setOperationThreading(BroadcastOperationThreading operationThreading) {
		request.operationThreading(operationThreading);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<RefreshResponse> listener) {
		client.refresh(request, listener);
	}
}
