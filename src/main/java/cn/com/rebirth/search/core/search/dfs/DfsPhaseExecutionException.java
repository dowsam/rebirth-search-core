/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DfsPhaseExecutionException.java 2012-7-6 14:30:05 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.dfs;

import cn.com.rebirth.search.core.search.SearchContextException;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class DfsPhaseExecutionException.
 *
 * @author l.xue.nong
 */
public class DfsPhaseExecutionException extends SearchContextException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7823246801033482641L;

	/**
	 * Instantiates a new dfs phase execution exception.
	 *
	 * @param context the context
	 * @param msg the msg
	 * @param t the t
	 */
	public DfsPhaseExecutionException(SearchContext context, String msg, Throwable t) {
		super(context, "Dfs Failed [" + msg + "]", t);
	}
}