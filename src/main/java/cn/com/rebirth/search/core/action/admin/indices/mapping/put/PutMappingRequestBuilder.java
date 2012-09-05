/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PutMappingRequestBuilder.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.mapping.put;

import java.util.Map;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.Required;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class PutMappingRequestBuilder.
 *
 * @author l.xue.nong
 */
public class PutMappingRequestBuilder extends BaseIndicesRequestBuilder<PutMappingRequest, PutMappingResponse> {

	/**
	 * Instantiates a new put mapping request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public PutMappingRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new PutMappingRequest());
	}

	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the put mapping request builder
	 */
	public PutMappingRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type
	 * @return the put mapping request builder
	 */
	@Required
	public PutMappingRequestBuilder setType(String type) {
		request.type(type);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param mappingBuilder the mapping builder
	 * @return the put mapping request builder
	 */
	public PutMappingRequestBuilder setSource(XContentBuilder mappingBuilder) {
		request.source(mappingBuilder);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param mappingSource the mapping source
	 * @return the put mapping request builder
	 */
	public PutMappingRequestBuilder setSource(Map mappingSource) {
		request.source(mappingSource);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param mappingSource the mapping source
	 * @return the put mapping request builder
	 */
	public PutMappingRequestBuilder setSource(String mappingSource) {
		request.source(mappingSource);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the put mapping request builder
	 */
	public PutMappingRequestBuilder setTimeout(TimeValue timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the put mapping request builder
	 */
	public PutMappingRequestBuilder setTimeout(String timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the put mapping request builder
	 */
	public PutMappingRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	/**
	 * Sets the ignore conflicts.
	 *
	 * @param ignoreConflicts the ignore conflicts
	 * @return the put mapping request builder
	 */
	public PutMappingRequestBuilder setIgnoreConflicts(boolean ignoreConflicts) {
		request.ignoreConflicts(ignoreConflicts);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<PutMappingResponse> listener) {
		client.putMapping(request, listener);
	}
}