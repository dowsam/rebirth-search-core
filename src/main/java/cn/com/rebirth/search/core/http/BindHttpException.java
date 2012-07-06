/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BindHttpException.java 2012-4-25 10:02:07 l.xue.nong$$
 */

package cn.com.rebirth.search.core.http;

/**
 * The Class BindHttpException.
 *
 * @author l.xue.nong
 */
public class BindHttpException extends HttpException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8139756466292161477L;

	/**
	 * Instantiates a new bind http exception.
	 *
	 * @param message the message
	 */
	public BindHttpException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new bind http exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public BindHttpException(String message, Throwable cause) {
		super(message, cause);
	}
}