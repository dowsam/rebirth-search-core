/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportSerializationException.java 2012-7-6 14:29:00 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

/**
 * The Class TransportSerializationException.
 *
 * @author l.xue.nong
 */
public class TransportSerializationException extends TransportException {

	/**
	 * Instantiates a new transport serialization exception.
	 *
	 * @param msg the msg
	 */
	public TransportSerializationException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new transport serialization exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public TransportSerializationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
