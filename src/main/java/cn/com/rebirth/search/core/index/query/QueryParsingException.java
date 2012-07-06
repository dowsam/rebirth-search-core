/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryParsingException.java 2012-7-6 14:29:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.IndexException;

/**
 * The Class QueryParsingException.
 *
 * @author l.xue.nong
 */
public class QueryParsingException extends IndexException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8302119892955758197L;

	/**
	 * Instantiates a new query parsing exception.
	 *
	 * @param index the index
	 * @param msg the msg
	 */
	public QueryParsingException(Index index, String msg) {
		super(index, msg);
	}

	/**
	 * Instantiates a new query parsing exception.
	 *
	 * @param index the index
	 * @param msg the msg
	 * @param cause the cause
	 */
	public QueryParsingException(Index index, String msg, Throwable cause) {
		super(index, msg, cause);
	}

}
