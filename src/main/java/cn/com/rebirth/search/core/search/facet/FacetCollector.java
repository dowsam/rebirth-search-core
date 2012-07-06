/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FacetCollector.java 2012-3-29 15:00:57 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;


/**
 * The Class FacetCollector.
 *
 * @author l.xue.nong
 */
public abstract class FacetCollector extends Collector {

	
	/**
	 * Facet.
	 *
	 * @return the facet
	 */
	public abstract Facet facet();

	
	/**
	 * Sets the filter.
	 *
	 * @param filter the new filter
	 */
	public abstract void setFilter(Filter filter);
}
