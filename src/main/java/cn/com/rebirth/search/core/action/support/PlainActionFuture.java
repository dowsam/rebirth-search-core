/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PlainActionFuture.java 2012-7-6 14:28:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support;

/**
 * The Class PlainActionFuture.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public class PlainActionFuture<T> extends AdapterActionFuture<T, T> {

	/**
	 * New future.
	 *
	 * @param <T> the generic type
	 * @return the plain action future
	 */
	public static <T> PlainActionFuture<T> newFuture() {
		return new PlainActionFuture<T>();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.AdapterActionFuture#convert(java.lang.Object)
	 */
	@Override
	protected T convert(T listenerResponse) {
		return listenerResponse;
	}
}
