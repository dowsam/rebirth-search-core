/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core JmxConnectorCreationException.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.jmx;

/**
 * The Class JmxConnectorCreationException.
 *
 * @author l.xue.nong
 */
public class JmxConnectorCreationException extends JmxException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7638694021399974018L;

	/**
	 * Instantiates a new jmx connector creation exception.
	 *
	 * @param message the message
	 */
	public JmxConnectorCreationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new jmx connector creation exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public JmxConnectorCreationException(String message, Throwable cause) {
		super(message, cause);
	}
}
