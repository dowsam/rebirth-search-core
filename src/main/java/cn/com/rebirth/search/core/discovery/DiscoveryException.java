/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DiscoveryException.java 2012-7-6 14:29:23 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class DiscoveryException.
 *
 * @author l.xue.nong
 */
public class DiscoveryException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5602425330312982330L;

	/**
	 * Instantiates a new discovery exception.
	 *
	 * @param message the message
	 */
	public DiscoveryException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new discovery exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public DiscoveryException(String message, Throwable cause) {
		super(message, cause);
	}
}
