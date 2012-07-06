/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FailedCommunicationException.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

/**
 * The Class FailedCommunicationException.
 *
 * @author l.xue.nong
 */
public class FailedCommunicationException extends TransportException {

	/**
	 * Instantiates a new failed communication exception.
	 *
	 * @param message the message
	 */
	public FailedCommunicationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new failed communication exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public FailedCommunicationException(String message, Throwable cause) {
		super(message, cause);
	}
}
