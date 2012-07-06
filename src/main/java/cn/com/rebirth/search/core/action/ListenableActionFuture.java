/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ListenableActionFuture.java 2012-7-6 14:29:05 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

/**
 * The Interface ListenableActionFuture.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface ListenableActionFuture<T> extends ActionFuture<T> {

	/**
	 * Adds the listener.
	 *
	 * @param listener the listener
	 */
	void addListener(final ActionListener<T> listener);

	/**
	 * Adds the listener.
	 *
	 * @param listener the listener
	 */
	void addListener(final Runnable listener);
}
