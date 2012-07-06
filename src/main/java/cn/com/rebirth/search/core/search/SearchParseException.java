/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchParseException.java 2012-7-6 14:30:16 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class SearchParseException.
 *
 * @author l.xue.nong
 */
public class SearchParseException extends SearchContextException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1867082159123322504L;

	/**
	 * Instantiates a new search parse exception.
	 *
	 * @param context the context
	 * @param msg the msg
	 */
	public SearchParseException(SearchContext context, String msg) {
		super(context, "Parse Failure [" + msg + "]");
	}

	/**
	 * Instantiates a new search parse exception.
	 *
	 * @param context the context
	 * @param msg the msg
	 * @param cause the cause
	 */
	public SearchParseException(SearchContext context, String msg, Throwable cause) {
		super(context, "Parse Failure [" + msg + "]", cause);
	}

}
