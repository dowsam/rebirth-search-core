/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GatewayException.java 2012-3-29 15:00:59 l.xue.nong$$
 */


package cn.com.rebirth.search.core.gateway;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class GatewayException.
 *
 * @author l.xue.nong
 */
public class GatewayException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7723514873497530289L;

	
	/**
	 * Instantiates a new gateway exception.
	 *
	 * @param msg the msg
	 */
	public GatewayException(String msg) {
		super(msg);
	}

	
	/**
	 * Instantiates a new gateway exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public GatewayException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
