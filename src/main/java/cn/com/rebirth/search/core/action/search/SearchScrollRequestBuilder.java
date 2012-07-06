/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchScrollRequestBuilder.java 2012-7-6 14:29:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.support.BaseRequestBuilder;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.search.Scroll;

/**
 * The Class SearchScrollRequestBuilder.
 *
 * @author l.xue.nong
 */
public class SearchScrollRequestBuilder extends BaseRequestBuilder<SearchScrollRequest, SearchResponse> {

	/**
	 * Instantiates a new search scroll request builder.
	 *
	 * @param client the client
	 */
	public SearchScrollRequestBuilder(Client client) {
		super(client, new SearchScrollRequest());
	}

	/**
	 * Instantiates a new search scroll request builder.
	 *
	 * @param client the client
	 * @param scrollId the scroll id
	 */
	public SearchScrollRequestBuilder(Client client, String scrollId) {
		super(client, new SearchScrollRequest(scrollId));
	}

	/**
	 * Sets the operation threading.
	 *
	 * @param operationThreading the operation threading
	 * @return the search scroll request builder
	 */
	public SearchScrollRequestBuilder setOperationThreading(SearchOperationThreading operationThreading) {
		request.operationThreading(operationThreading);
		return this;
	}

	/**
	 * Listener threaded.
	 *
	 * @param threadedListener the threaded listener
	 * @return the search scroll request builder
	 */
	public SearchScrollRequestBuilder listenerThreaded(boolean threadedListener) {
		request.listenerThreaded(threadedListener);
		return this;
	}

	/**
	 * Sets the scroll id.
	 *
	 * @param scrollId the scroll id
	 * @return the search scroll request builder
	 */
	public SearchScrollRequestBuilder setScrollId(String scrollId) {
		request.scrollId(scrollId);
		return this;
	}

	/**
	 * Sets the scroll.
	 *
	 * @param scroll the scroll
	 * @return the search scroll request builder
	 */
	public SearchScrollRequestBuilder setScroll(Scroll scroll) {
		request.scroll(scroll);
		return this;
	}

	/**
	 * Sets the scroll.
	 *
	 * @param keepAlive the keep alive
	 * @return the search scroll request builder
	 */
	public SearchScrollRequestBuilder setScroll(TimeValue keepAlive) {
		request.scroll(keepAlive);
		return this;
	}

	/**
	 * Sets the scroll.
	 *
	 * @param keepAlive the keep alive
	 * @return the search scroll request builder
	 */
	public SearchScrollRequestBuilder setScroll(String keepAlive) {
		request.scroll(keepAlive);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.BaseRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<SearchResponse> listener) {
		client.searchScroll(request, listener);
	}
}
