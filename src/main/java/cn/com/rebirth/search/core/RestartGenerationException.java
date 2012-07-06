/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SumMallSearchGenerationException.java 2012-3-29 15:01:02 l.xue.nong$$
 */


package cn.com.rebirth.search.core;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class SumMallSearchGenerationException.
 *
 * @author l.xue.nong
 */
public class RestartGenerationException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4635302866062095139L;

	
	/**
	 * Instantiates a new sum mall search generation exception.
	 *
	 * @param msg the msg
	 */
	public RestartGenerationException(String msg) {
		super(msg);
	}

	
	/**
	 * Instantiates a new sum mall search generation exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public RestartGenerationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
