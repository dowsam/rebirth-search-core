/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalTermsStatsFacet.java 2012-7-6 14:30:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.termsstats;

import java.util.List;

import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.InternalFacet;
import cn.com.rebirth.search.core.search.facet.termsstats.doubles.InternalTermsStatsDoubleFacet;
import cn.com.rebirth.search.core.search.facet.termsstats.longs.InternalTermsStatsLongFacet;
import cn.com.rebirth.search.core.search.facet.termsstats.strings.InternalTermsStatsStringFacet;

/**
 * The Class InternalTermsStatsFacet.
 *
 * @author l.xue.nong
 */
public abstract class InternalTermsStatsFacet implements TermsStatsFacet, InternalFacet {

	/**
	 * Register streams.
	 */
	public static void registerStreams() {
		InternalTermsStatsStringFacet.registerStream();
		InternalTermsStatsLongFacet.registerStream();
		InternalTermsStatsDoubleFacet.registerStream();
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