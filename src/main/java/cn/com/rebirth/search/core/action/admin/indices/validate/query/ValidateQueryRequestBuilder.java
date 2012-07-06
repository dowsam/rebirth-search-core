/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ValidateQueryRequestBuilder.java 2012-3-29 15:01:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.validate.query;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;
import cn.com.rebirth.search.core.client.IndicesAdminClient;
import cn.com.rebirth.search.core.index.query.QueryBuilder;


/**
 * The Class ValidateQueryRequestBuilder.
 *
 * @author l.xue.nong
 */
public class ValidateQueryRequestBuilder extends BaseIndicesRequestBuilder<ValidateQueryRequest, ValidateQueryResponse> {

	
	/**
	 * Instantiates a new validate query request builder.
	 *
	 * @param client the client
	 */
	public ValidateQueryRequestBuilder(IndicesAdminClient client) {
		super(client, new ValidateQueryRequest());
	}

	
	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the validate query request builder
	 */
	public ValidateQueryRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	
	/**
	 * Sets the types.
	 *
	 * @param types the types
	 * @return the validate query request builder
	 */
	public ValidateQueryRequestBuilder setTypes(String... types) {
		request.types(types);
		return this;
	}

	
	/**
	 * Sets the query.
	 *
	 * @param queryBuilder the query builder
	 * @return the validate query request builder
	 */
	public ValidateQueryRequestBuilder setQuery(QueryBuilder queryBuilder) {
		request.query(queryBuilder);
		return this;
	}

	
	/**
	 * Sets the query.
	 *
	 * @param querySource the query source
	 * @return the validate query request builder
	 */
	public ValidateQueryRequestBuilder setQuery(byte[] querySource) {
		request.query(querySource);
		return this;
	}

	
	/**
	 * Sets the operation threading.
	 *
	 * @param operationThreading the operation threading
	 * @return the validate query request builder
	 */
	public ValidateQueryRequestBuilder setOperationThreading(BroadcastOperationThreading operationThreading) {
		request.operationThreading(operationThreading);
		return this;
	}

	
	/**
	 * Sets the listener threaded.
	 *
	 * @param threadedListener the threaded listener
	 * @return the validate query request builder
	 */
	public ValidateQueryRequestBuilder setListenerThreaded(boolean threadedListener) {
		request.listenerThreaded(threadedListener);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<ValidateQueryResponse> listener) {
		client.validateQuery(request, listener);
	}
}
