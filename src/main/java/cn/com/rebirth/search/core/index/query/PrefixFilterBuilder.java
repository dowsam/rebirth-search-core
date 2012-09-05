/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PrefixFilterBuilder.java 2012-7-6 14:29:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.commons.xcontent.XContentBuilder;

/**
 * The Class PrefixFilterBuilder.
 *
 * @author l.xue.nong
 */
public class PrefixFilterBuilder extends BaseFilterBuilder {

	/** The name. */
	private final String name;

	/** The prefix. */
	private final String prefix;

	/** The cache. */
	private Boolean cache;

	/** The cache key. */
	private String cacheKey;

	/** The filter name. */
	private String filterName;

	/**
	 * Instantiates a new prefix filter builder.
	 *
	 * @param name the name
	 * @param prefix the prefix
	 */
	public PrefixFilterBuilder(String name, String prefix) {
		this.name = name;
		this.prefix = prefix;
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the prefix filter builder
	 */
	public PrefixFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the prefix filter builder
	 */
	public PrefixFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the prefix filter builder
	 */
	public PrefixFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(PrefixFilterParser.NAME);
		builder.field(name, prefix);
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