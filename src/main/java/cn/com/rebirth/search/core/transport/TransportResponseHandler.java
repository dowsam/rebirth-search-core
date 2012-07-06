/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportResponseHandler.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.io.stream.Streamable;

/**
 * The Interface TransportResponseHandler.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface TransportResponseHandler<T extends Streamable> {

	/**
	 * New instance.
	 *
	 * @return the t
	 */
	T newInstance();

	/**
	 * Handle response.
	 *
	 * @param response the response
	 */
	void handleResponse(T response);

	/**
	 * Handle exception.
	 *
	 * @param exp the exp
	 */
	void handleException(TransportException exp);

	/**
	 * Executor.
	 *
	 * @return the string
	 */
	String executor();
}
