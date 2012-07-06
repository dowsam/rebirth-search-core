/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core QueryFacet.java 2012-3-29 15:01:26 l.xue.nong$$
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