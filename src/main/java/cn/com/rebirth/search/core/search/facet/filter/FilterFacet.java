/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FilterFacet.java 2012-3-29 15:01:30 l.xue.nong$$
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