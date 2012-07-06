/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FetchPhaseExecutionException.java 2012-3-29 15:02:40 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.fetch;

import cn.com.rebirth.search.core.search.SearchContextException;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class FetchPhaseExecutionException.
 *
 * @author l.xue.nong
 */
public class FetchPhaseExecutionException extends SearchContextException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 367893011217690524L;

	
	/**
	 * Instantiates a new fetch phase execution exception.
	 *
	 * @param context the context
	 * @param msg the msg
	 */
	public FetchPhaseExecutionException(SearchContext context, String msg) {
		super(context, "Fetch Failed [" + msg + "]");
	}

	
	/**
	 * Instantiates a new fetch phase execution exception.
	 *
	 * @param context the context
	 * @param msg the msg
	 * @param t the t
	 */
	public FetchPhaseExecutionException(SearchContext context, String msg, Throwable t) {
		super(context, "Fetch Failed [" + msg + "]", t);
	}
}
