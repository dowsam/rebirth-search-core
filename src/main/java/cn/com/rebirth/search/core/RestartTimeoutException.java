/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestartTimeoutException.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class RestartTimeoutException.
 *
 * @author l.xue.nong
 */
public class RestartTimeoutException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1549008516991020282L;

	/**
	 * Instantiates a new restart timeout exception.
	 *
	 * @param message the message
	 */
	public RestartTimeoutException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new restart timeout exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public RestartTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}
}