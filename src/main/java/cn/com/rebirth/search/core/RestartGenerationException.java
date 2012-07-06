/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestartGenerationException.java 2012-7-6 14:29:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class RestartGenerationException.
 *
 * @author l.xue.nong
 */
public class RestartGenerationException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4635302866062095139L;

	/**
	 * Instantiates a new restart generation exception.
	 *
	 * @param msg the msg
	 */
	public RestartGenerationException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new restart generation exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public RestartGenerationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
