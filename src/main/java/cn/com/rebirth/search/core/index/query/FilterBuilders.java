/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FilterBuilders.java 2012-7-6 14:30:24 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import cn.com.rebirth.commons.Nullable;

/**
 * The Class FilterBuilders.
 *
 * @author l.xue.nong
 */
public abstract class FilterBuilders {

	/**
	 * Match all filter.
	 *
	 * @return the match all filter builder
	 */
	public static MatchAllFilterBuilder matchAllFilter() {
		return new MatchAllFilterBuilder();
	}

	/**
	 * Limit filter.
	 *
	 * @param limit the limit
	 * @return the limit filter builder
	 */
	public static LimitFilterBuilder limitFilter(int limit) {
		return new LimitFilterBuilder(limit);
	}

	/**
	 * Nested filter.
	 *
	 * @param path the path
	 * @param query the query
	 * @return the nested filter builder
	 */
	public static NestedFilterBuilder nestedFilter(String path, QueryBuilder query) {
		return new NestedFilterBuilder(path, query);
	}

	/**
	 * Nested filter.
	 *
	 * @param path the path
	 * @param filter the filter
	 * @return the nested filter builder
	 */
	public static NestedFilterBuilder nestedFilter(String path, FilterBuilder filter) {
		return new NestedFilterBuilder(path, filter);
	}

	/**
	 * Ids filter.
	 *
	 * @param types the types
	 * @return the ids filter builder
	 */
	public static IdsFilterBuilder idsFilter(@Nullable String... types) {
		return new IdsFilterBuilder(types);
	}

	/**
	 * Type filter.
	 *
	 * @param type the type
	 * @return the type filter builder
	 */
	public static TypeFilterBuilder typeFilter(String type) {
		return new TypeFilterBuilder(type);
	}

	/**
	 * Term filter.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the term filter builder
	 */
	public static TermFilterBuilder termFilter(String name, String value) {
		return new TermFilterBuilder(name, value);
	}

	/**
	 * Term filter.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the term filter builder
	 */
	public static TermFilterBuilder termFilter(String name, int value) {
		return new TermFilterBuilder(name, value);
	}

	/**
	 * Term filter.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the term filter builder
	 */
	public static TermFilterBuilder termFilter(String name, long value) {
		return new TermFilterBuilder(name, value);
	}

	/**
	 * Term filter.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the term filter builder
	 */
	public static TermFilterBuilder termFilter(String name, float value) {
		return new TermFilterBuilder(name, value);
	}

	/**
	 * Term filter.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the term filter builder
	 */
	public static TermFilterBuilder termFilter(String name, double value) {
		return new TermFilterBuilder(name, value);
	}

	/**
	 * Term filter.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the term filter builder
	 */
	public static TermFilterBuilder termFilter(String name, Object value) {
		return new TermFilterBuilder(name, value);
	}

	/**
	 * Terms filter.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms filter builder
	 */
	public static TermsFilterBuilder termsFilter(String name, String... values) {
		return new TermsFilterBuilder(name, values);
	}

	/**
	 * Terms filter.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms filter builder
	 */
	public static TermsFilterBuilder termsFilter(String name, int... values) {
		return new TermsFilterBuilder(name, values);
	}

	/**
	 * Terms filter.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms filter builder
	 */
	public static TermsFilterBuilder termsFilter(String name, long... values) {
		return new TermsFilterBuilder(name, values);
	}

	/**
	 * Terms filter.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms filter builder
	 */
	public static TermsFilterBuilder termsFilter(String name, float... values) {
		return new TermsFilterBuilder(name, values);
	}

	/**
	 * Terms filter.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms filter builder
	 */
	public static TermsFilterBuilder termsFilter(String name, double... values) {
		return new TermsFilterBuilder(name, values);
	}

	/**
	 * Terms filter.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms filter builder
	 */
	public static TermsFilterBuilder termsFilter(String name, Object... values) {
		return new TermsFilterBuilder(name, values);
	}

	/**
	 * Terms filter.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms filter builder
	 */
	public static TermsFilterBuilder termsFilter(String name, Iterable values) {
		return new TermsFilterBuilder(name, values);
	}

	/**
	 * In filter.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms filter builder
	 */
	public static TermsFilterBuilder inFilter(String name, String... values) {
		return new TermsFilterBuilder(name, values);
	}

	/**
	 * In filter.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms filter builder
	 */
	public static TermsFilterBuilder inFilter(String name, int... values) {
		return new TermsFilterBuilder(name, values);
	}

	/**
	 * In filter.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms filter builder
	 */
	public static TermsFilterBuilder inFilter(String name, long... values) {
		return new TermsFilterBuilder(name, values);
	}

	/**
	 * In filter.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms filter builder
	 */
	public static TermsFilterBuilder inFilter(String name, float... values) {
		return new TermsFilterBuilder(name, values);
	}

	/**
	 * In filter.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms filter builder
	 */
	public static TermsFilterBuilder inFilter(String name, double... values) {
		return new TermsFilterBuilder(name, values);
	}

	/**
	 * In filter.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms filter builder
	 */
	public static TermsFilterBuilder inFilter(String name, Object... values) {
		return new TermsFilterBuilder(name, values);
	}

	/**
	 * Prefix filter.
	 *
	 * @param name the name
	 * @param prefix the prefix
	 * @return the prefix filter builder
	 */
	public static PrefixFilterBuilder prefixFilter(String name, String prefix) {
		return new PrefixFilterBuilder(name, prefix);
	}

	/**
	 * Range filter.
	 *
	 * @param name the name
	 * @return the range filter builder
	 */
	public static RangeFilterBuilder rangeFilter(String name) {
		return new RangeFilterBuilder(name);
	}

	/**
	 * Numeric range filter.
	 *
	 * @param name the name
	 * @return the numeric range filter builder
	 */
	public static NumericRangeFilterBuilder numericRangeFilter(String name) {
		return new NumericRangeFilterBuilder(name);
	}

	/**
	 * Query filter.
	 *
	 * @param queryBuilder the query builder
	 * @return the query filter builder
	 */
	public static QueryFilterBuilder queryFilter(QueryBuilder queryBuilder) {
		return new QueryFilterBuilder(queryBuilder);
	}

	/**
	 * Script filter.
	 *
	 * @param script the script
	 * @return the script filter builder
	 */
	public static ScriptFilterBuilder scriptFilter(String script) {
		return new ScriptFilterBuilder(script);
	}

	/**
	 * Geo distance filter.
	 *
	 * @param name the name
	 * @return the geo distance filter builder
	 */
	public static GeoDistanceFilterBuilder geoDistanceFilter(String name) {
		return new GeoDistanceFilterBuilder(name);
	}

	/**
	 * Geo distance range filter.
	 *
	 * @param name the name
	 * @return the geo distance range filter builder
	 */
	public static GeoDistanceRangeFilterBuilder geoDistanceRangeFilter(String name) {
		return new GeoDistanceRangeFilterBuilder(name);
	}

	/**
	 * Geo bounding box filter.
	 *
	 * @param name the name
	 * @return the geo bounding box filter builder
	 */
	public static GeoBoundingBoxFilterBuilder geoBoundingBoxFilter(String name) {
		return new GeoBoundingBoxFilterBuilder(name);
	}

	/**
	 * Geo polygon filter.
	 *
	 * @param name the name
	 * @return the geo polygon filter builder
	 */
	public static GeoPolygonFilterBuilder geoPolygonFilter(String name) {
		return new GeoPolygonFilterBuilder(name);
	}

	/**
	 * Exists filter.
	 *
	 * @param name the name
	 * @return the exists filter builder
	 */
	public static ExistsFilterBuilder existsFilter(String name) {
		return new ExistsFilterBuilder(name);
	}

	/**
	 * Missing filter.
	 *
	 * @param name the name
	 * @return the missing filter builder
	 */
	public static MissingFilterBuilder missingFilter(String name) {
		return new MissingFilterBuilder(name);
	}

	/**
	 * Checks for child filter.
	 *
	 * @param type the type
	 * @param query the query
	 * @return the checks for child filter builder
	 */
	public static HasChildFilterBuilder hasChildFilter(String type, QueryBuilder query) {
		return new HasChildFilterBuilder(type, query);
	}

	/**
	 * Bool filter.
	 *
	 * @return the bool filter builder
	 */
	public static BoolFilterBuilder boolFilter() {
		return new BoolFilterBuilder();
	}

	/**
	 * And filter.
	 *
	 * @param filters the filters
	 * @return the and filter builder
	 */
	public static AndFilterBuilder andFilter(FilterBuilder... filters) {
		return new AndFilterBuilder(filters);
	}

	/**
	 * Or filter.
	 *
	 * @param filters the filters
	 * @return the or filter builder
	 */
	public static OrFilterBuilder orFilter(FilterBuilder... filters) {
		return new OrFilterBuilder(filters);
	}

	/**
	 * Not filter.
	 *
	 * @param filter the filter
	 * @return the not filter builder
	 */
	public static NotFilterBuilder notFilter(FilterBuilder filter) {
		return new NotFilterBuilder(filter);
	}

	/**
	 * Indices filter.
	 *
	 * @param filter the filter
	 * @param indices the indices
	 * @return the indices filter builder
	 */
	public static IndicesFilterBuilder indicesFilter(FilterBuilder filter, String... indices) {
		return new IndicesFilterBuilder(filter, indices);
	}

	/**
	 * Instantiates a new filter builders.
	 */
	private FilterBuilders() {

	}
}
