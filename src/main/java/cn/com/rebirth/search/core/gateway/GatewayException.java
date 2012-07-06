/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GatewayException.java 2012-7-6 14:29:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class GatewayException.
 *
 * @author l.xue.nong
 */
public class GatewayException extends RebirthException {

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
