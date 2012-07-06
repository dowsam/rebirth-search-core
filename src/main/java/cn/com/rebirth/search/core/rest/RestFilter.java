/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestFilter.java 2012-7-6 14:30:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.search.commons.component.CloseableComponent;

/**
 * The Class RestFilter.
 *
 * @author l.xue.nong
 */
public abstract class RestFilter implements CloseableComponent {

	/**
	 * Order.
	 *
	 * @return the int
	 */
	public int order() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() throws RebirthException {

	}

	/**
	 * Process.
	 *
	 * @param request the request
	 * @param channel the channel
	 * @param filterChain the filter chain
	 */
	public abstract void process(RestRequest request, RestChannel channel, RestFilterChain filterChain);
}
