/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RangeFilterBuilder.java 2012-3-29 15:01:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class RangeFilterBuilder.
 *
 * @author l.xue.nong
 */
public class RangeFilterBuilder extends BaseFilterBuilder {

	
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
	 * Instantiates a new range filter builder.
	 *
	 * @param name the name
	 */
	public RangeFilterBuilder(String name) {
		this.name = name;
	}

	
	/**
	 * From.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder from(Object from) {
		this.from = from;
		return this;
	}

	
	/**
	 * From.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder from(int from) {
		this.from = from;
		return this;
	}

	
	/**
	 * From.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder from(long from) {
		this.from = from;
		return this;
	}

	
	/**
	 * From.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder from(float from) {
		this.from = from;
		return this;
	}

	
	/**
	 * From.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder from(double from) {
		this.from = from;
		return this;
	}

	
	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder gt(Object from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	
	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder gt(int from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	
	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder gt(long from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	
	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder gt(float from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	
	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder gt(double from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	
	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder gte(Object from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	
	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder gte(int from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	
	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder gte(long from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	
	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder gte(float from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	
	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the range filter builder
	 */
	public RangeFilterBuilder gte(double from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	
	/**
	 * To.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder to(Object to) {
		this.to = to;
		return this;
	}

	
	/**
	 * To.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder to(int to) {
		this.to = to;
		return this;
	}

	
	/**
	 * To.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder to(long to) {
		this.to = to;
		return this;
	}

	
	/**
	 * To.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder to(float to) {
		this.to = to;
		return this;
	}

	
	/**
	 * To.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder to(double to) {
		this.to = to;
		return this;
	}

	
	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder lt(Object to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	
	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder lt(int to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	
	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder lt(long to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	
	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder lt(float to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	
	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder lt(double to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	
	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder lte(int to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	
	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder lte(long to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	
	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder lte(float to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	
	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder lte(double to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	
	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the range filter builder
	 */
	public RangeFilterBuilder lte(Object to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	
	/**
	 * Include lower.
	 *
	 * @param includeLower the include lower
	 * @return the range filter builder
	 */
	public RangeFilterBuilder includeLower(boolean includeLower) {
		this.includeLower = includeLower;
		return this;
	}

	
	/**
	 * Include upper.
	 *
	 * @param includeUpper the include upper
	 * @return the range filter builder
	 */
	public RangeFilterBuilder includeUpper(boolean includeUpper) {
		this.includeUpper = includeUpper;
		return this;
	}

	
	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the range filter builder
	 */
	public RangeFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	
	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the range filter builder
	 */
	public RangeFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	
	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the range filter builder
	 */
	public RangeFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(RangeFilterParser.NAME);

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