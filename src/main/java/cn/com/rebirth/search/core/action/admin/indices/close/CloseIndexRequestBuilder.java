/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CloseIndexRequestBuilder.java 2012-7-6 14:30:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.close;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class CloseIndexRequestBuilder.
 *
 * @author l.xue.nong
 */
public class CloseIndexRequestBuilder extends BaseIndicesRequestBuilder<CloseIndexRequest, CloseIndexResponse> {

	/**
	 * Instantiates a new close index request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public CloseIndexRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new CloseIndexRequest());
	}

	/**
	 * Instantiates a new close index request builder.
	 *
	 * @param indicesClient the indices client
	 * @param index the index
	 */
	public CloseIndexRequestBuilder(IndicesAdminClient indicesClient, String index) {
		super(indicesClient, new CloseIndexRequest(index));
	}

	/**
	 * Sets the index.
	 *
	 * @param index the index
	 * @return the close index request builder
	 */
	public CloseIndexRequestBuilder setIndex(String index) {
		request.index(index);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the close index request builder
	 */
	public CloseIndexRequestBuilder setTimeout(TimeValue timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the close index request builder
	 */
	public CloseIndexRequestBuilder setTimeout(String timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the close index request builder
	 */
	public CloseIndexRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the close index request builder
	 */
	public CloseIndexRequestBuilder setMasterNodeTimeout(String timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<CloseIndexResponse> listener) {
		client.close(request, listener);
	}
}
