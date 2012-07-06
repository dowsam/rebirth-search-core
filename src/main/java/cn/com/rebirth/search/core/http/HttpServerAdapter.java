/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core HttpServerAdapter.java 2012-4-25 10:02:42 l.xue.nong$$
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
