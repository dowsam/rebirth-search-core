/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ActionFuture.java 2012-3-29 15:02:17 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RestartException;
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
	 * @throws SumMallSearchException the sum mall search exception
	 */
	T actionGet() throws RestartException;

	
	/**
	 * Action get.
	 *
	 * @param timeout the timeout
	 * @return the t
	 * @throws SumMallSearchException the sum mall search exception
	 */
	T actionGet(String timeout) throws RestartException;

	
	/**
	 * Action get.
	 *
	 * @param timeoutMillis the timeout millis
	 * @return the t
	 * @throws SumMallSearchException the sum mall search exception
	 */
	T actionGet(long timeoutMillis) throws RestartException;

	
	/**
	 * Action get.
	 *
	 * @param timeout the timeout
	 * @param unit the unit
	 * @return the t
	 * @throws SumMallSearchException the sum mall search exception
	 */
	T actionGet(long timeout, TimeUnit unit) throws RestartException;

	
	/**
	 * Action get.
	 *
	 * @param timeout the timeout
	 * @return the t
	 * @throws SumMallSearchException the sum mall search exception
	 */
	T actionGet(TimeValue timeout) throws RestartException;

	
	/**
	 * Gets the root failure.
	 *
	 * @return the root failure
	 */
	@Nullable
	Throwable getRootFailure();
}
