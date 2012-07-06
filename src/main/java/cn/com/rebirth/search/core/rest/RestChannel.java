/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestChannel.java 2012-3-29 15:02:31 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest;


/**
 * The Interface RestChannel.
 *
 * @author l.xue.nong
 */
public interface RestChannel {

	
	/**
	 * Send response.
	 *
	 * @param response the response
	 */
	void sendResponse(RestResponse response);
}