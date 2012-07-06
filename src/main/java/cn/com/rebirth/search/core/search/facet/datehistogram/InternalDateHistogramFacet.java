/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalDateHistogramFacet.java 2012-3-29 15:01:54 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.datehistogram;

import java.util.List;

import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.InternalFacet;


/**
 * The Class InternalDateHistogramFacet.
 *
 * @author l.xue.nong
 */
public abstract class InternalDateHistogramFacet implements DateHistogramFacet, InternalFacet {

	
	/**
	 * Register streams.
	 */
	public static void registerStreams() {
		InternalCountDateHistogramFacet.registerStreams();
		InternalFullDateHistogramFacet.registerStreams();
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
