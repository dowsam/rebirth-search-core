/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PercolateIndexUnavailable.java 2012-7-6 14:30:01 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.percolator;

import cn.com.rebirth.search.core.index.Index;

/**
 * The Class PercolateIndexUnavailable.
 *
 * @author l.xue.nong
 */
public class PercolateIndexUnavailable extends PercolatorException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5766589764020975815L;

	/**
	 * Instantiates a new percolate index unavailable.
	 *
	 * @param index the index
	 */
	public PercolateIndexUnavailable(Index index) {
		super(index, "percolator index not allocated on this node");
	}
}
