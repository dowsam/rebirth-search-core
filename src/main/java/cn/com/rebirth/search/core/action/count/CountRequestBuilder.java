/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CountRequestBuilder.java 2012-3-29 15:02:29 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.count;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.support.BaseRequestBuilder;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.index.query.QueryBuilder;


/**
 * The Class CountRequestBuilder.
 *
 * @author l.xue.nong
 */
public class CountRequestBuilder extends BaseRequestBuilder<CountRequest, CountResponse> {

	
	/**
	 * Instantiates a new count request builder.
	 *
	 * @param client the client
	 */
	public CountRequestBuilder(Client client) {
		super(client, new CountRequest());
	}

	
	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the count request builder
	 */
	public CountRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	
	/**
	 * Sets the types.
	 *
	 * @param types the types
	 * @return the count request builder
	 */
	public CountRequestBuilder setTypes(String... types) {
		request.types(types);
		return this;
	}

	
	/**
	 * Sets the min score.
	 *
	 * @param minScore the min score
	 * @return the count request builder
	 */
	public CountRequestBuilder setMinScore(float minScore) {
		request.minScore(minScore);
		return this;
	}

	
	/**
	 * Sets the query hint.
	 *
	 * @param queryHint the query hint
	 * @return the count request builder
	 */
	public CountRequestBuilder setQueryHint(String queryHint) {
		request.queryHint(queryHint);
		return this;
	}

	
	/**
	 * Sets the routing.
	 *
	 * @param routing the routing
	 * @return the count request builder
	 */
	public CountRequestBuilder setRouting(String routing) {
		request.routing(routing);
		return this;
	}

	
	/**
	 * Sets the routing.
	 *
	 * @param routing the routing
	 * @return the count request builder
	 */
	public CountRequestBuilder setRouting(String... routing) {
		request.routing(routing);
		return this;
	}

	
	/**
	 * Sets the query.
	 *
	 * @param queryBuilder the query builder
	 * @return the count request builder
	 */
	public CountRequestBuilder setQuery(QueryBuilder queryBuilder) {
		request.query(queryBuilder);
		return this;
	}

	
	/**
	 * Sets the query.
	 *
	 * @param querySource the query source
	 * @return the count request builder
	 */
	public CountRequestBuilder setQuery(byte[] querySource) {
		request.query(querySource);
		return this;
	}

	
	/**
	 * Sets the operation threading.
	 *
	 * @param operationThreading the operation threading
	 * @return the count request builder
	 */
	public CountRequestBuilder setOperationThreading(BroadcastOperationThreading operationThreading) {
		request.operationThreading(operationThreading);
		return this;
	}

	
	/**
	 * Sets the listener threaded.
	 *
	 * @param threadedListener the threaded listener
	 * @return the count request builder
	 */
	public CountRequestBuilder setListenerThreaded(boolean threadedListener) {
		request.listenerThreaded(threadedListener);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.BaseRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<CountResponse> listener) {
		client.count(request, listener);
	}
}
