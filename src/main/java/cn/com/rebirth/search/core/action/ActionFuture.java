/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ActionFuture.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.unit.TimeValue;

/**
 * The Interface ActionFuture.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface ActionFuture<T> extends Future<T> {

	/**
	 * Action get.
	 *
	 * @return the t
	 * @throws RebirthException the rebirth exception
	 */
	T actionGet() throws RebirthException;

	/**
	 * Action get.
	 *
	 * @param timeout the timeout
	 * @return the t
	 * @throws RebirthException the rebirth exception
	 */
	T actionGet(String timeout) throws RebirthException;

	/**
	 * Action get.
	 *
	 * @param timeoutMillis the timeout millis
	 * @return the t
	 * @throws RebirthException the rebirth exception
	 */
	T actionGet(long timeoutMillis) throws RebirthException;

	/**
	 * Action get.
	 *
	 * @param timeout the timeout
	 * @param unit the unit
	 * @return the t
	 * @throws RebirthException the rebirth exception
	 */
	T actionGet(long timeout, TimeUnit unit) throws RebirthException;

	/**
	 * Action get.
	 *
	 * @param timeout the timeout
	 * @return the t
	 * @throws RebirthException the rebirth exception
	 */
	T actionGet(TimeValue timeout) throws RebirthException;

	/**
	 * Gets the root failure.
	 *
	 * @return the root failure
	 */
	@Nullable
	Throwable getRootFailure();
}
