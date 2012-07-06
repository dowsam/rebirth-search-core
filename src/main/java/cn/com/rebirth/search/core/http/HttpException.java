/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HttpException.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.http;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class HttpException.
 *
 * @author l.xue.nong
 */
public class HttpException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2636958198881031774L;

	/**
	 * Instantiates a new http exception.
	 *
	 * @param message the message
	 */
	public HttpException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new http exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public HttpException(String message, Throwable cause) {
		super(message, cause);
	}
}