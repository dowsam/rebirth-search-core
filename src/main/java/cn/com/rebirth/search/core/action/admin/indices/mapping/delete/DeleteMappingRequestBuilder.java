/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DeleteMappingRequestBuilder.java 2012-7-6 14:30:16 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.mapping.delete;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class DeleteMappingRequestBuilder.
 *
 * @author l.xue.nong
 */
public class DeleteMappingRequestBuilder extends BaseIndicesRequestBuilder<DeleteMappingRequest, DeleteMappingResponse> {

	/**
	 * Instantiates a new delete mapping request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public DeleteMappingRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new DeleteMappingRequest());
	}

	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the delete mapping request builder
	 */
	public DeleteMappingRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type
	 * @return the delete mapping request builder
	 */
	public DeleteMappingRequestBuilder setType(String type) {
		request.type(type);
		return this;
	}

	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete mapping request builder
	 */
	public DeleteMappingRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<DeleteMappingResponse> listener) {
		client.deleteMapping(request, listener);
	}
}