/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BindTransportException.java 2012-7-6 14:30:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

/**
 * The Class BindTransportException.
 *
 * @author l.xue.nong
 */
public class BindTransportException extends TransportException {

	/**
	 * Instantiates a new bind transport exception.
	 *
	 * @param message the message
	 */
	public BindTransportException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new bind transport exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public BindTransportException(String message, Throwable cause) {
		super(message, cause);
	}
}