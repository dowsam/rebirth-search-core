/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexException.java 2012-3-29 15:02:03 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class IndexException.
 *
 * @author l.xue.nong
 */
public class IndexException extends RestartException {

	
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
