/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FacetCollector.java 2012-7-6 14:30:33 l.xue.nong$$
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
