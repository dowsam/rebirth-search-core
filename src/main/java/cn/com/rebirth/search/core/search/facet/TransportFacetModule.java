/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportFacetModule.java 2012-7-6 14:29:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet;

import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.search.facet.datehistogram.InternalDateHistogramFacet;
import cn.com.rebirth.search.core.search.facet.filter.InternalFilterFacet;
import cn.com.rebirth.search.core.search.facet.geodistance.InternalGeoDistanceFacet;
import cn.com.rebirth.search.core.search.facet.histogram.InternalHistogramFacet;
import cn.com.rebirth.search.core.search.facet.query.InternalQueryFacet;
import cn.com.rebirth.search.core.search.facet.range.InternalRangeFacet;
import cn.com.rebirth.search.core.search.facet.statistical.InternalStatisticalFacet;
import cn.com.rebirth.search.core.search.facet.terms.InternalTermsFacet;
import cn.com.rebirth.search.core.search.facet.termsstats.InternalTermsStatsFacet;

/**
 * The Class TransportFacetModule.
 *
 * @author l.xue.nong
 */
public class TransportFacetModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		InternalFilterFacet.registerStreams();
		InternalQueryFacet.registerStreams();
		InternalGeoDistanceFacet.registerStreams();
		InternalHistogramFacet.registerStreams();
		InternalDateHistogramFacet.registerStreams();
		InternalRangeFacet.registerStreams();
		InternalStatisticalFacet.registerStreams();
		InternalTermsFacet.registerStreams();
		InternalTermsStatsFacet.registerStreams();
	}
}
