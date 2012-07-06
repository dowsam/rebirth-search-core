/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NotFilterBuilder.java 2012-3-29 15:01:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class NotFilterBuilder.
 *
 * @author l.xue.nong
 */
public class NotFilterBuilder extends BaseFilterBuilder {

	
	/** The filter. */
	private FilterBuilder filter;

	
	/** The cache. */
	private Boolean cache;

	
	/** The filter name. */
	private String filterName;

	
	/**
	 * Instantiates a new not filter builder.
	 *
	 * @param filter the filter
	 */
	public NotFilterBuilder(FilterBuilder filter) {
		this.filter = filter;
	}

	
	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the not filter builder
	 */
	public NotFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	
	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the not filter builder
	 */
	public NotFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(NotFilterParser.NAME);
		builder.field("filter");
		filter.toXContent(builder, params);
		if (cache != null) {
			builder.field("_cache", cache);
		}
		if (filterName != null) {
			builder.field("_name", filterName);
		}
		builder.endObject();
	}
}