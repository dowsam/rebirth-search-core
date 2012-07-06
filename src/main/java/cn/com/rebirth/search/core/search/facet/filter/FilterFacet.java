/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FilterFacet.java 2012-7-6 14:29:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.filter;

import cn.com.rebirth.search.core.search.facet.Facet;

/**
 * The Interface FilterFacet.
 *
 * @author l.xue.nong
 */
public interface FilterFacet extends Facet {

	/** The Constant TYPE. */
	public static final String TYPE = "filter";

	/**
	 * Count.
	 *
	 * @return the long
	 */
	long count();

	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	long getCount();
}