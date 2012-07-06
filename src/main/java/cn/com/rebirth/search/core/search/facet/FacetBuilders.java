/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FacetBuilders.java 2012-3-29 15:01:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet;

import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.index.query.QueryBuilder;
import cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacetBuilder;
import cn.com.rebirth.search.core.search.facet.filter.FilterFacetBuilder;
import cn.com.rebirth.search.core.search.facet.geodistance.GeoDistanceFacetBuilder;
import cn.com.rebirth.search.core.search.facet.histogram.HistogramFacetBuilder;
import cn.com.rebirth.search.core.search.facet.histogram.HistogramScriptFacetBuilder;
import cn.com.rebirth.search.core.search.facet.query.QueryFacetBuilder;
import cn.com.rebirth.search.core.search.facet.range.RangeFacetBuilder;
import cn.com.rebirth.search.core.search.facet.range.RangeScriptFacetBuilder;
import cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacetBuilder;
import cn.com.rebirth.search.core.search.facet.statistical.StatisticalScriptFacetBuilder;
import cn.com.rebirth.search.core.search.facet.terms.TermsFacetBuilder;
import cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacetBuilder;


/**
 * The Class FacetBuilders.
 *
 * @author l.xue.nong
 */
public class FacetBuilders {

	
	/**
	 * Query facet.
	 *
	 * @param facetName the facet name
	 * @return the query facet builder
	 */
	public static QueryFacetBuilder queryFacet(String facetName) {
		return new QueryFacetBuilder(facetName);
	}

	
	/**
	 * Query facet.
	 *
	 * @param facetName the facet name
	 * @param query the query
	 * @return the query facet builder
	 */
	public static QueryFacetBuilder queryFacet(String facetName, QueryBuilder query) {
		return new QueryFacetBuilder(facetName).query(query);
	}

	
	/**
	 * Filter facet.
	 *
	 * @param facetName the facet name
	 * @return the filter facet builder
	 */
	public static FilterFacetBuilder filterFacet(String facetName) {
		return new FilterFacetBuilder(facetName);
	}

	
	/**
	 * Filter facet.
	 *
	 * @param facetName the facet name
	 * @param filter the filter
	 * @return the filter facet builder
	 */
	public static FilterFacetBuilder filterFacet(String facetName, FilterBuilder filter) {
		return new FilterFacetBuilder(facetName).filter(filter);
	}

	
	/**
	 * Terms facet.
	 *
	 * @param facetName the facet name
	 * @return the terms facet builder
	 */
	public static TermsFacetBuilder termsFacet(String facetName) {
		return new TermsFacetBuilder(facetName);
	}

	
	/**
	 * Terms stats facet.
	 *
	 * @param facetName the facet name
	 * @return the terms stats facet builder
	 */
	public static TermsStatsFacetBuilder termsStatsFacet(String facetName) {
		return new TermsStatsFacetBuilder(facetName);
	}

	
	/**
	 * Statistical facet.
	 *
	 * @param facetName the facet name
	 * @return the statistical facet builder
	 */
	public static StatisticalFacetBuilder statisticalFacet(String facetName) {
		return new StatisticalFacetBuilder(facetName);
	}

	
	/**
	 * Statistical script facet.
	 *
	 * @param facetName the facet name
	 * @return the statistical script facet builder
	 */
	public static StatisticalScriptFacetBuilder statisticalScriptFacet(String facetName) {
		return new StatisticalScriptFacetBuilder(facetName);
	}

	
	/**
	 * Histogram facet.
	 *
	 * @param facetName the facet name
	 * @return the histogram facet builder
	 */
	public static HistogramFacetBuilder histogramFacet(String facetName) {
		return new HistogramFacetBuilder(facetName);
	}

	
	/**
	 * Date histogram facet.
	 *
	 * @param facetName the facet name
	 * @return the date histogram facet builder
	 */
	public static DateHistogramFacetBuilder dateHistogramFacet(String facetName) {
		return new DateHistogramFacetBuilder(facetName);
	}

	
	/**
	 * Histogram script facet.
	 *
	 * @param facetName the facet name
	 * @return the histogram script facet builder
	 */
	public static HistogramScriptFacetBuilder histogramScriptFacet(String facetName) {
		return new HistogramScriptFacetBuilder(facetName);
	}

	
	/**
	 * Range facet.
	 *
	 * @param facetName the facet name
	 * @return the range facet builder
	 */
	public static RangeFacetBuilder rangeFacet(String facetName) {
		return new RangeFacetBuilder(facetName);
	}

	
	/**
	 * Range script facet.
	 *
	 * @param facetName the facet name
	 * @return the range script facet builder
	 */
	public static RangeScriptFacetBuilder rangeScriptFacet(String facetName) {
		return new RangeScriptFacetBuilder(facetName);
	}

	
	/**
	 * Geo distance facet.
	 *
	 * @param facetName the facet name
	 * @return the geo distance facet builder
	 */
	public static GeoDistanceFacetBuilder geoDistanceFacet(String facetName) {
		return new GeoDistanceFacetBuilder(facetName);
	}
}
