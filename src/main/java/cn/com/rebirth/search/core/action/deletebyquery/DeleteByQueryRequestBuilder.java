/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DeleteByQueryRequestBuilder.java 2012-3-29 15:02:54 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.deletebyquery;

import java.util.Map;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.support.BaseRequestBuilder;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.index.query.QueryBuilder;


/**
 * The Class DeleteByQueryRequestBuilder.
 *
 * @author l.xue.nong
 */
public class DeleteByQueryRequestBuilder extends BaseRequestBuilder<DeleteByQueryRequest, DeleteByQueryResponse> {

	
	/**
	 * Instantiates a new delete by query request builder.
	 *
	 * @param client the client
	 */
	public DeleteByQueryRequestBuilder(Client client) {
		super(client, new DeleteByQueryRequest());
	}

	
	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	
	/**
	 * Sets the types.
	 *
	 * @param types the types
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setTypes(String... types) {
		request.types(types);
		return this;
	}

	
	/**
	 * Sets the routing.
	 *
	 * @param routing the routing
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setRouting(String routing) {
		request.routing(routing);
		return this;
	}

	
	/**
	 * Sets the routing.
	 *
	 * @param routing the routing
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setRouting(String... routing) {
		request.routing(routing);
		return this;
	}

	
	/**
	 * Sets the query.
	 *
	 * @param queryBuilder the query builder
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setQuery(QueryBuilder queryBuilder) {
		request.query(queryBuilder);
		return this;
	}

	
	/**
	 * Sets the query.
	 *
	 * @param querySource the query source
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setQuery(String querySource) {
		request.query(querySource);
		return this;
	}

	
	/**
	 * Sets the query.
	 *
	 * @param querySource the query source
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setQuery(Map<String, Object> querySource) {
		request.query(querySource);
		return this;
	}

	
	/**
	 * Sets the query.
	 *
	 * @param builder the builder
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setQuery(XContentBuilder builder) {
		request.query(builder);
		return this;
	}

	
	/**
	 * Sets the query.
	 *
	 * @param querySource the query source
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setQuery(byte[] querySource) {
		request.query(querySource);
		return this;
	}

	
	/**
	 * Sets the query.
	 *
	 * @param querySource the query source
	 * @param offset the offset
	 * @param length the length
	 * @param unsafe the unsafe
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setQuery(byte[] querySource, int offset, int length, boolean unsafe) {
		request.query(querySource, offset, length, unsafe);
		return this;
	}

	
	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setTimeout(TimeValue timeout) {
		request.timeout(timeout);
		return this;
	}

	
	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setTimeout(String timeout) {
		request.timeout(timeout);
		return this;
	}

	
	/**
	 * Sets the replication type.
	 *
	 * @param replicationType the replication type
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setReplicationType(ReplicationType replicationType) {
		request.replicationType(replicationType);
		return this;
	}

	
	/**
	 * Sets the replication type.
	 *
	 * @param replicationType the replication type
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setReplicationType(String replicationType) {
		request.replicationType(replicationType);
		return this;
	}

	
	/**
	 * Sets the consistency level.
	 *
	 * @param consistencyLevel the consistency level
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setConsistencyLevel(WriteConsistencyLevel consistencyLevel) {
		request.consistencyLevel(consistencyLevel);
		return this;
	}

	
	/**
	 * Sets the listener threaded.
	 *
	 * @param threadedListener the threaded listener
	 * @return the delete by query request builder
	 */
	public DeleteByQueryRequestBuilder setListenerThreaded(boolean threadedListener) {
		request.listenerThreaded(threadedListener);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.BaseRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<DeleteByQueryResponse> listener) {
		client.deleteByQuery(request, listener);
	}
}
