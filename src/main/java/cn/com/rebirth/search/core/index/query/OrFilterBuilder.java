/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core OrFilterBuilder.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.ArrayList;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

import com.google.common.collect.Lists;

/**
 * The Class OrFilterBuilder.
 *
 * @author l.xue.nong
 */
public class OrFilterBuilder extends BaseFilterBuilder {

	/** The filters. */
	private ArrayList<FilterBuilder> filters = Lists.newArrayList();

	/** The cache. */
	private Boolean cache;

	/** The cache key. */
	private String cacheKey;

	/** The filter name. */
	private String filterName;

	/**
	 * Instantiates a new or filter builder.
	 *
	 * @param filters the filters
	 */
	public OrFilterBuilder(FilterBuilder... filters) {
		for (FilterBuilder filter : filters) {
			this.filters.add(filter);
		}
	}

	/**
	 * Adds the.
	 *
	 * @param filterBuilder the filter builder
	 * @return the or filter builder
	 */
	public OrFilterBuilder add(FilterBuilder filterBuilder) {
		filters.add(filterBuilder);
		return this;
	}

	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the or filter builder
	 */
	public OrFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the or filter builder
	 */
	public OrFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the or filter builder
	 */
	public OrFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(OrFilterParser.NAME);
		builder.startArray("filters");
		for (FilterBuilder filter : filters) {
			filter.toXContent(builder, params);
		}
		builder.endArray();
		if (cache != null) {
			builder.field("_cache", cache);
		}
		if (cacheKey != null) {
			builder.field("_cache_key", cacheKey);
		}
		if (filterName != null) {
			builder.field("_name", filterName);
		}
		builder.endObject();
	}
}