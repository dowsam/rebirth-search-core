/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DeleteIndexRequestBuilder.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.delete;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class DeleteIndexRequestBuilder.
 *
 * @author l.xue.nong
 */
public class DeleteIndexRequestBuilder extends BaseIndicesRequestBuilder<DeleteIndexRequest, DeleteIndexResponse> {

	/**
	 * Instantiates a new delete index request builder.
	 *
	 * @param indicesClient the indices client
	 * @param indices the indices
	 */
	public DeleteIndexRequestBuilder(IndicesAdminClient indicesClient, String... indices) {
		super(indicesClient, new DeleteIndexRequest(indices));
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete index request builder
	 */
	public DeleteIndexRequestBuilder setTimeout(TimeValue timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete index request builder
	 */
	public DeleteIndexRequestBuilder setTimeout(String timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete index request builder
	 */
	public DeleteIndexRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete index request builder
	 */
	public DeleteIndexRequestBuilder setMasterNodeTimeout(String timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<DeleteIndexResponse> listener) {
		client.delete(request, listener);
	}
}
