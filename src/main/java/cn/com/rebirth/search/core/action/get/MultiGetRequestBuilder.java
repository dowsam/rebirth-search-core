/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MultiGetRequestBuilder.java 2012-7-6 14:29:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.get;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.support.BaseRequestBuilder;
import cn.com.rebirth.search.core.client.Client;

/**
 * The Class MultiGetRequestBuilder.
 *
 * @author l.xue.nong
 */
public class MultiGetRequestBuilder extends BaseRequestBuilder<MultiGetRequest, MultiGetResponse> {

	/**
	 * Instantiates a new multi get request builder.
	 *
	 * @param client the client
	 */
	public MultiGetRequestBuilder(Client client) {
		super(client, new MultiGetRequest());
	}

	/**
	 * Adds the.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @return the multi get request builder
	 */
	public MultiGetRequestBuilder add(String index, @Nullable String type, String id) {
		request.add(index, type, id);
		return this;
	}

	/**
	 * Adds the.
	 *
	 * @param index the index
	 * @param type the type
	 * @param ids the ids
	 * @return the multi get request builder
	 */
	public MultiGetRequestBuilder add(String index, @Nullable String type, Iterable<String> ids) {
		for (String id : ids) {
			request.add(index, type, id);
		}
		return this;
	}

	/**
	 * Adds the.
	 *
	 * @param index the index
	 * @param type the type
	 * @param ids the ids
	 * @return the multi get request builder
	 */
	public MultiGetRequestBuilder add(String index, @Nullable String type, String... ids) {
		for (String id : ids) {
			request.add(index, type, id);
		}
		return this;
	}

	/**
	 * Adds the.
	 *
	 * @param item the item
	 * @return the multi get request builder
	 */
	public MultiGetRequestBuilder add(MultiGetRequest.Item item) {
		request.add(item);
		return this;
	}

	/**
	 * Sets the preference.
	 *
	 * @param preference the preference
	 * @return the multi get request builder
	 */
	public MultiGetRequestBuilder setPreference(String preference) {
		request.preference(preference);
		return this;
	}

	/**
	 * Sets the refresh.
	 *
	 * @param refresh the refresh
	 * @return the multi get request builder
	 */
	public MultiGetRequestBuilder setRefresh(boolean refresh) {
		request.refresh(refresh);
		return this;
	}

	/**
	 * Sets the realtime.
	 *
	 * @param realtime the realtime
	 * @return the multi get request builder
	 */
	public MultiGetRequestBuilder setRealtime(Boolean realtime) {
		request.realtime(realtime);
		return this;
	}

	/**
	 * Sets the listener threaded.
	 *
	 * @param threadedListener the threaded listener
	 * @return the multi get request builder
	 */
	public MultiGetRequestBuilder setListenerThreaded(boolean threadedListener) {
		request.listenerThreaded(threadedListener);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.BaseRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<MultiGetResponse> listener) {
		client.multiGet(request, listener);
	}
}
