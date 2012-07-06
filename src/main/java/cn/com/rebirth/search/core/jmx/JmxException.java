/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core JmxException.java 2012-7-6 14:28:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.jmx;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class JmxException.
 *
 * @author l.xue.nong
 */
public class JmxException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3687382001364684809L;

	/**
	 * Instantiates a new jmx exception.
	 *
	 * @param message the message
	 */
	public JmxException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new jmx exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public JmxException(String message, Throwable cause) {
		super(message, cause);
	}
}