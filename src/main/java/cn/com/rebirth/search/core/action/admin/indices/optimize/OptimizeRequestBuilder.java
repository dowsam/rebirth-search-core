/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core OptimizeRequestBuilder.java 2012-7-6 14:29:53 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.optimize;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class OptimizeRequestBuilder.
 *
 * @author l.xue.nong
 */
public class OptimizeRequestBuilder extends BaseIndicesRequestBuilder<OptimizeRequest, OptimizeResponse> {

	/**
	 * Instantiates a new optimize request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public OptimizeRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new OptimizeRequest());
	}

	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the optimize request builder
	 */
	public OptimizeRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	/**
	 * Sets the wait for merge.
	 *
	 * @param waitForMerge the wait for merge
	 * @return the optimize request builder
	 */
	public OptimizeRequestBuilder setWaitForMerge(boolean waitForMerge) {
		request.waitForMerge(waitForMerge);
		return this;
	}

	/**
	 * Sets the max num segments.
	 *
	 * @param maxNumSegments the max num segments
	 * @return the optimize request builder
	 */
	public OptimizeRequestBuilder setMaxNumSegments(int maxNumSegments) {
		request.maxNumSegments(maxNumSegments);
		return this;
	}

	/**
	 * Sets the only expunge deletes.
	 *
	 * @param onlyExpungeDeletes the only expunge deletes
	 * @return the optimize request builder
	 */
	public OptimizeRequestBuilder setOnlyExpungeDeletes(boolean onlyExpungeDeletes) {
		request.onlyExpungeDeletes(onlyExpungeDeletes);
		return this;
	}

	/**
	 * Sets the flush.
	 *
	 * @param flush the flush
	 * @return the optimize request builder
	 */
	public OptimizeRequestBuilder setFlush(boolean flush) {
		request.flush(flush);
		return this;
	}

	/**
	 * Sets the refresh.
	 *
	 * @param refresh the refresh
	 * @return the optimize request builder
	 */
	public OptimizeRequestBuilder setRefresh(boolean refresh) {
		request.refresh(refresh);
		return this;
	}

	/**
	 * Sets the listener threaded.
	 *
	 * @param threadedListener the threaded listener
	 * @return the optimize request builder
	 */
	public OptimizeRequestBuilder setListenerThreaded(boolean threadedListener) {
		request.listenerThreaded(threadedListener);
		return this;
	}

	/**
	 * Sets the operation threading.
	 *
	 * @param operationThreading the operation threading
	 * @return the optimize request builder
	 */
	public OptimizeRequestBuilder setOperationThreading(BroadcastOperationThreading operationThreading) {
		request.operationThreading(operationThreading);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<OptimizeResponse> listener) {
		client.optimize(request, listener);
	}
}
