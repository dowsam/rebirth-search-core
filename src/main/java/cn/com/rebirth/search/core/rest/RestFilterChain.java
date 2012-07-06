/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestFilterChain.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest;

/**
 * The Interface RestFilterChain.
 *
 * @author l.xue.nong
 */
public interface RestFilterChain {

	/**
	 * Continue processing.
	 *
	 * @param request the request
	 * @param channel the channel
	 */
	void continueProcessing(RestRequest request, RestChannel channel);
}
