/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestChannel.java 2012-7-6 14:30:25 l.xue.nong$$
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