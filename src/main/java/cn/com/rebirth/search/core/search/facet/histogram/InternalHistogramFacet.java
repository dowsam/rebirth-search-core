/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalHistogramFacet.java 2012-7-6 14:30:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.histogram;

import java.util.List;

import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.InternalFacet;
import cn.com.rebirth.search.core.search.facet.histogram.bounded.InternalBoundedCountHistogramFacet;
import cn.com.rebirth.search.core.search.facet.histogram.bounded.InternalBoundedFullHistogramFacet;
import cn.com.rebirth.search.core.search.facet.histogram.unbounded.InternalCountHistogramFacet;
import cn.com.rebirth.search.core.search.facet.histogram.unbounded.InternalFullHistogramFacet;

/**
 * The Class InternalHistogramFacet.
 *
 * @author l.xue.nong
 */
public abstract class InternalHistogramFacet implements HistogramFacet, InternalFacet {

	/**
	 * Register streams.
	 */
	public static void registerStreams() {
		InternalFullHistogramFacet.registerStreams();
		InternalCountHistogramFacet.registerStreams();
		InternalBoundedCountHistogramFacet.registerStreams();
		InternalBoundedFullHistogramFacet.registerStreams();
	}

	/**
	 * Reduce.
	 *
	 * @param name the name
	 * @param facets the facets
	 * @return the facet
	 */
	public abstract Facet reduce(String name, List<Facet> facets);
}
