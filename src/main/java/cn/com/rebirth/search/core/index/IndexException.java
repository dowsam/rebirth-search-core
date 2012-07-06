/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexException.java 2012-7-6 14:29:51 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class IndexException.
 *
 * @author l.xue.nong
 */
public class IndexException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7995033790903153938L;

	/** The index. */
	private final Index index;

	/**
	 * Instantiates a new index exception.
	 *
	 * @param index the index
	 * @param msg the msg
	 */
	public IndexException(Index index, String msg) {
		this(index, msg, null);
	}

	/**
	 * Instantiates a new index exception.
	 *
	 * @param index the index
	 * @param msg the msg
	 * @param cause the cause
	 */
	public IndexException(Index index, String msg, Throwable cause) {
		this(index, true, msg, cause);
	}

	/**
	 * Instantiates a new index exception.
	 *
	 * @param index the index
	 * @param withSpace the with space
	 * @param msg the msg
	 * @param cause the cause
	 */
	protected IndexException(Index index, boolean withSpace, String msg, Throwable cause) {
		super("[" + (index == null ? "_na" : index.name()) + "]" + (withSpace ? " " : "") + msg, cause);
		this.index = index;
	}

	/**
	 * Index.
	 *
	 * @return the index
	 */
	public Index index() {
		return index;
	}
}
