/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SumMallSearchTimeoutException.java 2012-3-29 15:02:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class SumMallSearchTimeoutException.
 *
 * @author l.xue.nong
 */
public class RestartTimeoutException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1549008516991020282L;

	
	/**
	 * Instantiates a new sum mall search timeout exception.
	 *
	 * @param message the message
	 */
	public RestartTimeoutException(String message) {
		super(message);
	}

	
	/**
	 * Instantiates a new sum mall search timeout exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public RestartTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}
}