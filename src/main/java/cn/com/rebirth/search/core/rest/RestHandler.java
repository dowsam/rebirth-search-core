/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestHandler.java 2012-3-29 15:02:03 l.xue.nong$$
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