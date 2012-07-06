/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ActionTransportRequestHandler.java 2012-7-6 14:28:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.io.stream.Streamable;

/**
 * The Interface ActionTransportRequestHandler.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface ActionTransportRequestHandler<T extends Streamable> extends TransportRequestHandler<T> {

	/**
	 * Action.
	 *
	 * @return the string
	 */
	String action();
}