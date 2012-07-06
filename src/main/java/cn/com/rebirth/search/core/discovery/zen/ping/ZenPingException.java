/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ZenPingException.java 2012-7-6 14:29:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery.zen.ping;

import cn.com.rebirth.search.core.discovery.DiscoveryException;

/**
 * The Class ZenPingException.
 *
 * @author l.xue.nong
 */
public class ZenPingException extends DiscoveryException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 472826406523647401L;

	/**
	 * Instantiates a new zen ping exception.
	 *
	 * @param message the message
	 */
	public ZenPingException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new zen ping exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public ZenPingException(String message, Throwable cause) {
		super(message, cause);
	}
}
