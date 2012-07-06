/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportFuture.java 2012-3-29 15:01:23 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Interface TransportFuture.
 *
 * @param <V> the value type
 * @author l.xue.nong
 */
public interface TransportFuture<V> extends Future<V> {

	
	/**
	 * Tx get.
	 *
	 * @return the v
	 * @throws SumMallSearchException the sum mall search exception
	 */
	V txGet() throws RestartException;

	
	/**
	 * Tx get.
	 *
	 * @param timeout the timeout
	 * @param unit the unit
	 * @return the v
	 * @throws SumMallSearchException the sum mall search exception
	 */
	V txGet(long timeout, TimeUnit unit) throws RestartException;
}
