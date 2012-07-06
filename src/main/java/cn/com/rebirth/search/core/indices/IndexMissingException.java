/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexMissingException.java 2012-7-6 14:29:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.IndexException;

/**
 * The Class IndexMissingException.
 *
 * @author l.xue.nong
 */
public class IndexMissingException extends IndexException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1007256881642021231L;

	/**
	 * Instantiates a new index missing exception.
	 *
	 * @param index the index
	 */
	public IndexMissingException(Index index) {
		super(index, "missing");
	}

}