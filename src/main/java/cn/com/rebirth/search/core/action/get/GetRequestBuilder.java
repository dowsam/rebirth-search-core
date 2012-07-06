/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GetRequestBuilder.java 2012-7-6 14:28:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.get;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.support.BaseRequestBuilder;
import cn.com.rebirth.search.core.client.Client;

/**
 * The Class GetRequestBuilder.
 *
 * @author l.xue.nong
 */
public class GetRequestBuilder extends BaseRequestBuilder<GetRequest, GetResponse> {

	/**
	 * Instantiates a new gets the request builder.
	 *
	 * @param client the client
	 */
	public GetRequestBuilder(Client client) {
		super(client, new GetRequest());
	}

	/**
	 * Instantiates a new gets the request builder.
	 *
	 * @param client the client
	 * @param index the index
	 */
	public GetRequestBuilder(Client client, @Nullable String index) {
		super(client, new GetRequest(index));
	}

	/**
	 * Sets the index.
	 *
	 * @param index the index
	 * @return the gets the request builder
	 */
	public GetRequestBuilder setIndex(String index) {
		request.index(index);
		return this;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type
	 * @return the gets the request builder
	 */
	public GetRequestBuilder setType(@Nullable String type) {
		request.type(type);
		return this;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the id
	 * @return the gets the request builder
	 */
	public GetRequestBuilder setId(String id) {
		request.id(id);
		return this;
	}

	/**
	 * Sets the routing.
	 *
	 * @param routing the routing
	 * @return the gets the request builder
	 */
	public GetRequestBuilder setRouting(String routing) {
		request.routing(routing);
		return this;
	}

	/**
	 * Sets the preference.
	 *
	 * @param preference the preference
	 * @return the gets the request builder
	 */
	public GetRequestBuilder setPreference(String preference) {
		request.preference(preference);
		return this;
	}

	/**
	 * Sets the fields.
	 *
	 * @param fields the fields
	 * @return the gets the request builder
	 */
	public GetRequestBuilder setFields(String... fields) {
		request.fields(fields);
		return this;
	}

	/**
	 * Sets the refresh.
	 *
	 * @param refresh the refresh
	 * @return the gets the request builder
	 */
	public GetRequestBuilder setRefresh(boolean refresh) {
		request.refresh(refresh);
		return this;
	}

	/**
	 * Sets the realtime.
	 *
	 * @param realtime the realtime
	 * @return the gets the request builder
	 */
	public GetRequestBuilder setRealtime(Boolean realtime) {
		request.realtime(realtime);
		return this;
	}

	/**
	 * Sets the listener threaded.
	 *
	 * @param threadedListener the threaded listener
	 * @return the gets the request builder
	 */
	public GetRequestBuilder setListenerThreaded(boolean threadedListener) {
		request.listenerThreaded(threadedListener);
		return this;
	}

	/**
	 * Sets the operation threaded.
	 *
	 * @param threadedOperation the threaded operation
	 * @return the gets the request builder
	 */
	public GetRequestBuilder setOperationThreaded(boolean threadedOperation) {
		request.operationThreaded(threadedOperation);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.BaseRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<GetResponse> listener) {
		client.get(request, listener);
	}
}
