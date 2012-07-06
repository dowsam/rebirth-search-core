/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NumericRangeFilterBuilder.java 2012-7-6 14:29:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class NumericRangeFilterBuilder.
 *
 * @author l.xue.nong
 */
public class NumericRangeFilterBuilder extends BaseFilterBuilder {

	/** The name. */
	private final String name;

	/** The from. */
	private Object from;

	/** The to. */
	private Object to;

	/** The include lower. */
	private boolean includeLower = true;

	/** The include upper. */
	private boolean includeUpper = true;

	/** The cache. */
	private Boolean cache;

	/** The cache key. */
	private String cacheKey;

	/** The filter name. */
	private String filterName;

	/**
	 * Instantiates a new numeric range filter builder.
	 *
	 * @param name the name
	 */
	public NumericRangeFilterBuilder(String name) {
		this.name = name;
	}

	/**
	 * From.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder from(Object from) {
		this.from = from;
		return this;
	}

	/**
	 * From.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder from(int from) {
		this.from = from;
		return this;
	}

	/**
	 * From.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder from(long from) {
		this.from = from;
		return this;
	}

	/**
	 * From.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder from(float from) {
		this.from = from;
		return this;
	}

	/**
	 * From.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder from(double from) {
		this.from = from;
		return this;
	}

	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder gt(Object from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder gt(int from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder gt(long from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder gt(float from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder gt(double from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder gte(Object from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder gte(int from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder gte(long from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder gte(float from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder gte(double from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	/**
	 * To.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder to(Object to) {
		this.to = to;
		return this;
	}

	/**
	 * To.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder to(int to) {
		this.to = to;
		return this;
	}

	/**
	 * To.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder to(long to) {
		this.to = to;
		return this;
	}

	/**
	 * To.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder to(float to) {
		this.to = to;
		return this;
	}

	/**
	 * To.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder to(double to) {
		this.to = to;
		return this;
	}

	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder lt(Object to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder lt(int to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder lt(long to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder lt(float to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder lt(double to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder lte(Object to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder lte(int to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder lte(long to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder lte(float to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder lte(double to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	/**
	 * Include lower.
	 *
	 * @param includeLower the include lower
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder includeLower(boolean includeLower) {
		this.includeLower = includeLower;
		return this;
	}

	/**
	 * Include upper.
	 *
	 * @param includeUpper the include upper
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder includeUpper(boolean includeUpper) {
		this.includeUpper = includeUpper;
		return this;
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the numeric range filter builder
	 */
	public NumericRangeFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(NumericRangeFilterParser.NAME);

		builder.startObject(name);
		builder.field("from", from);
		builder.field("to", to);
		builder.field("include_lower", includeLower);
		builder.field("include_upper", includeUpper);
		builder.endObject();

		if (filterName != null) {
			builder.field("_name", filterName);
		}
		if (cache != null) {
			builder.field("_cache", cache);
		}
		if (cacheKey != null) {
			builder.field("_cache_key", cacheKey);
		}

		builder.endObject();
	}
}