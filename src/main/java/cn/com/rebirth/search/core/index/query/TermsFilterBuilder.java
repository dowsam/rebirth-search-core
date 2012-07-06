/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermsFilterBuilder.java 2012-7-6 14:29:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class TermsFilterBuilder.
 *
 * @author l.xue.nong
 */
public class TermsFilterBuilder extends BaseFilterBuilder {

	/** The name. */
	private final String name;

	/** The values. */
	private final Object values;

	/** The cache. */
	private Boolean cache;

	/** The cache key. */
	private String cacheKey;

	/** The filter name. */
	private String filterName;

	/** The execution. */
	private String execution;

	/**
	 * Instantiates a new terms filter builder.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public TermsFilterBuilder(String name, String... values) {
		this(name, (Object[]) values);
	}

	/**
	 * Instantiates a new terms filter builder.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public TermsFilterBuilder(String name, int... values) {
		this.name = name;
		this.values = values;
	}

	/**
	 * Instantiates a new terms filter builder.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public TermsFilterBuilder(String name, long... values) {
		this.name = name;
		this.values = values;
	}

	/**
	 * Instantiates a new terms filter builder.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public TermsFilterBuilder(String name, float... values) {
		this.name = name;
		this.values = values;
	}

	/**
	 * Instantiates a new terms filter builder.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public TermsFilterBuilder(String name, double... values) {
		this.name = name;
		this.values = values;
	}

	/**
	 * Instantiates a new terms filter builder.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public TermsFilterBuilder(String name, Object... values) {
		this.name = name;
		this.values = values;
	}

	/**
	 * Instantiates a new terms filter builder.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public TermsFilterBuilder(String name, Iterable values) {
		this.name = name;
		this.values = values;
	}

	/**
	 * Execution.
	 *
	 * @param execution the execution
	 * @return the terms filter builder
	 */
	public TermsFilterBuilder execution(String execution) {
		this.execution = execution;
		return this;
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the terms filter builder
	 */
	public TermsFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the terms filter builder
	 */
	public TermsFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the terms filter builder
	 */
	public TermsFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(TermsFilterParser.NAME);
		builder.field(name, values);

		if (execution != null) {
			builder.field("execution", execution);
		}

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