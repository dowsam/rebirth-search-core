/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core JmxRegistrationException.java 2012-7-6 14:29:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.jmx;

/**
 * The Class JmxRegistrationException.
 *
 * @author l.xue.nong
 */
public class JmxRegistrationException extends JmxException {

	/**
	 * Instantiates a new jmx registration exception.
	 *
	 * @param message the message
	 */
	public JmxRegistrationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new jmx registration exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public JmxRegistrationException(String message, Throwable cause) {
		super(message, cause);
	}
}