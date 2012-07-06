/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchServiceListener.java 2012-7-6 14:29:49 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.action;

/**
 * The listener interface for receiving searchService events.
 * The class that is interested in processing a searchService
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addSearchServiceListener<code> method. When
 * the searchService event occurs, that object's appropriate
 * method is invoked.
 *
 * @param <T> the generic type
 * @see SearchServiceEvent
 */
public interface SearchServiceListener<T> {

	/**
	 * On result.
	 *
	 * @param result the result
	 */
	void onResult(T result);

	/**
	 * On failure.
	 *
	 * @param t the t
	 */
	void onFailure(Throwable t);
}
