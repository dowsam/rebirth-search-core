/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TermFilterBuilder.java 2012-3-29 15:02:43 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class TermFilterBuilder.
 *
 * @author l.xue.nong
 */
public class TermFilterBuilder extends BaseFilterBuilder {

	
	/** The name. */
	private final String name;

	
	/** The value. */
	private final Object value;

	
	/** The cache. */
	private Boolean cache;

	
	/** The cache key. */
	private String cacheKey;

	
	/** The filter name. */
	private String filterName;

	
	/**
	 * Instantiates a new term filter builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public TermFilterBuilder(String name, String value) {
		this(name, (Object) value);
	}

	
	/**
	 * Instantiates a new term filter builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public TermFilterBuilder(String name, int value) {
		this(name, (Object) value);
	}

	
	/**
	 * Instantiates a new term filter builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public TermFilterBuilder(String name, long value) {
		this(name, (Object) value);
	}

	
	/**
	 * Instantiates a new term filter builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public TermFilterBuilder(String name, float value) {
		this(name, (Object) value);
	}

	
	/**
	 * Instantiates a new term filter builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public TermFilterBuilder(String name, double value) {
		this(name, (Object) value);
	}

	
	/**
	 * Instantiates a new term filter builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public TermFilterBuilder(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	
	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the term filter builder
	 */
	public TermFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	
	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the term filter builder
	 */
	public TermFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	
	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the term filter builder
	 */
	public TermFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(TermFilterParser.NAME);
		builder.field(name, value);
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