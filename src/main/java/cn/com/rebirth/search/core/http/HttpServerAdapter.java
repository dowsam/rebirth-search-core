/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HttpServerAdapter.java 2012-7-6 14:29:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.http;

/**
 * The Interface HttpServerAdapter.
 *
 * @author l.xue.nong
 */
public interface HttpServerAdapter {

	/**
	 * Dispatch request.
	 *
	 * @param request the request
	 * @param channel the channel
	 */
	void dispatchRequest(HttpRequest request, HttpChannel channel);
}
