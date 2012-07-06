/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexAlreadyExistsException.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.IndexException;

/**
 * The Class IndexAlreadyExistsException.
 *
 * @author l.xue.nong
 */
public class IndexAlreadyExistsException extends IndexException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2014242766563318251L;

	/**
	 * Instantiates a new index already exists exception.
	 *
	 * @param index the index
	 */
	public IndexAlreadyExistsException(Index index) {
		super(index, "Already exists");
	}

}