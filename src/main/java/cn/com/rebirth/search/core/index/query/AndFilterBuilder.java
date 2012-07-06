/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AndFilterBuilder.java 2012-3-29 15:02:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.ArrayList;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

import com.google.common.collect.Lists;


/**
 * The Class AndFilterBuilder.
 *
 * @author l.xue.nong
 */
public class AndFilterBuilder extends BaseFilterBuilder {

	
	/** The filters. */
	private ArrayList<FilterBuilder> filters = Lists.newArrayList();

	
	/** The cache. */
	private Boolean cache;

	
	/** The cache key. */
	private String cacheKey;

	
	/** The filter name. */
	private String filterName;

	
	/**
	 * Instantiates a new and filter builder.
	 *
	 * @param filters the filters
	 */
	public AndFilterBuilder(FilterBuilder... filters) {
		for (FilterBuilder filter : filters) {
			this.filters.add(filter);
		}
	}

	
	/**
	 * Adds the.
	 *
	 * @param filterBuilder the filter builder
	 * @return the and filter builder
	 */
	public AndFilterBuilder add(FilterBuilder filterBuilder) {
		filters.add(filterBuilder);
		return this;
	}

	
	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the and filter builder
	 */
	public AndFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	
	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the and filter builder
	 */
	public AndFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	
	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the and filter builder
	 */
	public AndFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(AndFilterParser.NAME);
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