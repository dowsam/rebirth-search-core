/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchContextFacets.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet;

import java.util.List;

/**
 * The Class SearchContextFacets.
 *
 * @author l.xue.nong
 */
public class SearchContextFacets {

	/** The facet collectors. */
	private final List<FacetCollector> facetCollectors;

	/**
	 * Instantiates a new search context facets.
	 *
	 * @param facetCollectors the facet collectors
	 */
	public SearchContextFacets(List<FacetCollector> facetCollectors) {
		this.facetCollectors = facetCollectors;
	}

	/**
	 * Facet collectors.
	 *
	 * @return the list
	 */
	public List<FacetCollector> facetCollectors() {
		return facetCollectors;
	}
}
