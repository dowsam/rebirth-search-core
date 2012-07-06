/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestFilter.java 2012-3-29 15:02:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest;

import cn.com.rebirth.commons.exception.RestartException;
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
	 * @see cn.com.summall.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() throws RestartException {

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
