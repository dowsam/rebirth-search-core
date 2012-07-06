/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PercolatorException.java 2012-3-29 15:00:58 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.percolator;

import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.IndexException;


/**
 * The Class PercolatorException.
 *
 * @author l.xue.nong
 */
public class PercolatorException extends IndexException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3848733038658314245L;

	
	/**
	 * Instantiates a new percolator exception.
	 *
	 * @param index the index
	 * @param msg the msg
	 */
	public PercolatorException(Index index, String msg) {
		super(index, msg);
	}

	
	/**
	 * Instantiates a new percolator exception.
	 *
	 * @param index the index
	 * @param msg the msg
	 * @param cause the cause
	 */
	public PercolatorException(Index index, String msg, Throwable cause) {
		super(index, msg, cause);
	}
}
