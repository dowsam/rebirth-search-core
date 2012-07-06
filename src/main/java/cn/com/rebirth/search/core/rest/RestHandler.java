/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestHandler.java 2012-7-6 14:30:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest;

/**
 * The Interface RestHandler.
 *
 * @author l.xue.nong
 */
public interface RestHandler {

	/**
	 * Handle request.
	 *
	 * @param request the request
	 * @param channel the channel
	 */
	void handleRequest(RestRequest request, RestChannel channel);
}