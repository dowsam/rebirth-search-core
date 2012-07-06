/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryBuilderException.java 2012-7-6 14:30:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class QueryBuilderException.
 *
 * @author l.xue.nong
 */
public class QueryBuilderException extends RebirthException {

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
