/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportFuture.java 2012-7-6 14:30:23 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.exception.RebirthException;

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
	 * @throws RebirthException the rebirth exception
	 */
	V txGet() throws RebirthException;

	/**
	 * Tx get.
	 *
	 * @param timeout the timeout
	 * @param unit the unit
	 * @return the v
	 * @throws RebirthException the rebirth exception
	 */
	V txGet(long timeout, TimeUnit unit) throws RebirthException;
}
