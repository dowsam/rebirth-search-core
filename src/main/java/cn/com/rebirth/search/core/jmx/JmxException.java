/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core JmxException.java 2012-3-29 15:02:39 l.xue.nong$$
 */


package cn.com.rebirth.search.core.jmx;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class JmxException.
 *
 * @author l.xue.nong
 */
public class JmxException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3687382001364684809L;

	
	/**
	 * Instantiates a new jmx exception.
	 *
	 * @param message the message
	 */
	public JmxException(String message) {
		super(message);
	}

	
	/**
	 * Instantiates a new jmx exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public JmxException(String message, Throwable cause) {
		super(message, cause);
	}
}