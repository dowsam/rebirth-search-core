/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core QueryPhaseExecutionException.java 2012-3-29 15:01:38 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.query;

import cn.com.rebirth.search.core.search.SearchContextException;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class QueryPhaseExecutionException.
 *
 * @author l.xue.nong
 */
public class QueryPhaseExecutionException extends SearchContextException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2670469922921433398L;

	
	/**
	 * Instantiates a new query phase execution exception.
	 *
	 * @param context the context
	 * @param msg the msg
	 * @param cause the cause
	 */
	public QueryPhaseExecutionException(SearchContext context, String msg, Throwable cause) {
		super(context, "Query Failed [" + msg + "]", cause);
	}
}
