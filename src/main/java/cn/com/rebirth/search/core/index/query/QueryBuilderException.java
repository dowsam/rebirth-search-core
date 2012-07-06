/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core QueryBuilderException.java 2012-3-29 15:01:08 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class QueryBuilderException.
 *
 * @author l.xue.nong
 */
public class QueryBuilderException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4570302971892504079L;

	
	/**
	 * Instantiates a new query builder exception.
	 *
	 * @param msg the msg
	 */
	public QueryBuilderException(String msg) {
		super(msg);
	}

	
	/**
	 * Instantiates a new query builder exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public QueryBuilderException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
