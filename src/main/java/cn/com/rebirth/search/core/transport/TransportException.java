/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportException.java 2012-7-6 14:30:15 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class TransportException.
 *
 * @author l.xue.nong
 */
public class TransportException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6300393876815181484L;

	/**
	 * Instantiates a new transport exception.
	 *
	 * @param msg the msg
	 */
	public TransportException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new transport exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public TransportException(String msg, Throwable cause) {
		super(msg, cause);
	}
}