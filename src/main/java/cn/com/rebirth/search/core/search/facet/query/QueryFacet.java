/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryFacet.java 2012-7-6 14:30:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.query;

import cn.com.rebirth.search.core.search.facet.Facet;

/**
 * The Interface QueryFacet.
 *
 * @author l.xue.nong
 */
public interface QueryFacet extends Facet {

	/** The Constant TYPE. */
	public static final String TYPE = "query";

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