/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RoutingException.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class RoutingException.
 *
 * @author l.xue.nong
 */
public class RoutingException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7463550232449843250L;

	
	/**
	 * Instantiates a new routing exception.
	 *
	 * @param message the message
	 */
	public RoutingException(String message) {
		super(message);
	}

	
	/**
	 * Instantiates a new routing exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public RoutingException(String message, Throwable cause) {
		super(message, cause);
	}
}