/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexCreationException.java 2012-7-6 14:29:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.IndexException;

/**
 * The Class IndexCreationException.
 *
 * @author l.xue.nong
 */
public class IndexCreationException extends IndexException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4426307787247868886L;

	/**
	 * Instantiates a new index creation exception.
	 *
	 * @param index the index
	 * @param cause the cause
	 */
	public IndexCreationException(Index index, Throwable cause) {
		super(index, "failed to create index", cause);
	}
}
