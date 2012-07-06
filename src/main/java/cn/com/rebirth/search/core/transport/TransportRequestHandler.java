/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportRequestHandler.java 2012-7-6 14:29:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.io.stream.Streamable;

/**
 * The Interface TransportRequestHandler.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface TransportRequestHandler<T extends Streamable> {

	/**
	 * New instance.
	 *
	 * @return the t
	 */
	T newInstance();

	/**
	 * Message received.
	 *
	 * @param request the request
	 * @param channel the channel
	 * @throws Exception the exception
	 */
	void messageReceived(T request, TransportChannel channel) throws Exception;

	/**
	 * Executor.
	 *
	 * @return the string
	 */
	String executor();
}
