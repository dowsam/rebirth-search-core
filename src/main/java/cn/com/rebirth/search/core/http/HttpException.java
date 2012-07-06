/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core HttpException.java 2012-4-25 10:02:14 l.xue.nong$$
 */


package cn.com.rebirth.search.core.http;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class HttpException.
 *
 * @author l.xue.nong
 */
public class HttpException extends RestartException {

	
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