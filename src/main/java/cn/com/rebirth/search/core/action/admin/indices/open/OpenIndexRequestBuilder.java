/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core OpenIndexRequestBuilder.java 2012-7-6 14:29:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.open;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class OpenIndexRequestBuilder.
 *
 * @author l.xue.nong
 */
public class OpenIndexRequestBuilder extends BaseIndicesRequestBuilder<OpenIndexRequest, OpenIndexResponse> {

	/**
	 * Instantiates a new open index request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public OpenIndexRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new OpenIndexRequest());
	}

	/**
	 * Instantiates a new open index request builder.
	 *
	 * @param indicesClient the indices client
	 * @param index the index
	 */
	public OpenIndexRequestBuilder(IndicesAdminClient indicesClient, String index) {
		super(indicesClient, new OpenIndexRequest(index));
	}

	/**
	 * Sets the index.
	 *
	 * @param index the index
	 * @return the open index request builder
	 */
	public OpenIndexRequestBuilder setIndex(String index) {
		request.index(index);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the open index request builder
	 */
	public OpenIndexRequestBuilder setTimeout(TimeValue timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the open index request builder
	 */
	public OpenIndexRequestBuilder setTimeout(String timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the open index request builder
	 */
	public OpenIndexRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the open index request builder
	 */
	public OpenIndexRequestBuilder setMasterNodeTimeout(String timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<OpenIndexResponse> listener) {
		client.open(request, listener);
	}
}
