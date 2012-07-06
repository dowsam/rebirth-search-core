/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchSourceBuilderException.java 2012-3-29 15:00:59 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.builder;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class SearchSourceBuilderException.
 *
 * @author l.xue.nong
 */
public class SearchSourceBuilderException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7005527804441845801L;

	
	/**
	 * Instantiates a new search source builder exception.
	 *
	 * @param msg the msg
	 */
	public SearchSourceBuilderException(String msg) {
		super(msg);
	}

	
	/**
	 * Instantiates a new search source builder exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public SearchSourceBuilderException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
