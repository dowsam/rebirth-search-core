/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NestedFilterBuilder.java 2012-3-29 15:01:50 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class NestedFilterBuilder.
 *
 * @author l.xue.nong
 */
public class NestedFilterBuilder extends BaseFilterBuilder {

	
	/** The query builder. */
	private final QueryBuilder queryBuilder;

	
	/** The filter builder. */
	private final FilterBuilder filterBuilder;

	
	/** The path. */
	private final String path;

	
	/** The scope. */
	private String scope;

	
	/** The cache. */
	private Boolean cache;

	
	/** The cache key. */
	private String cacheKey;

	
	/** The filter name. */
	private String filterName;

	
	/**
	 * Instantiates a new nested filter builder.
	 *
	 * @param path the path
	 * @param queryBuilder the query builder
	 */
	public NestedFilterBuilder(String path, QueryBuilder queryBuilder) {
		this.path = path;
		this.queryBuilder = queryBuilder;
		this.filterBuilder = null;
	}

	
	/**
	 * Instantiates a new nested filter builder.
	 *
	 * @param path the path
	 * @param filterBuilder the filter builder
	 */
	public NestedFilterBuilder(String path, FilterBuilder filterBuilder) {
		this.path = path;
		this.queryBuilder = null;
		this.filterBuilder = filterBuilder;
	}

	
	/**
	 * Scope.
	 *
	 * @param scope the scope
	 * @return the nested filter builder
	 */
	public NestedFilterBuilder scope(String scope) {
		this.scope = scope;
		return this;
	}

	
	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the nested filter builder
	 */
	public NestedFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	
	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the nested filter builder
	 */
	public NestedFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	
	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the nested filter builder
	 */
	public NestedFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(NestedFilterParser.NAME);
		if (queryBuilder != null) {
			builder.field("query");
			queryBuilder.toXContent(builder, params);
		} else {
			builder.field("filter");
			filterBuilder.toXContent(builder, params);
		}
		builder.field("path", path);
		if (scope != null) {
			builder.field("_scope", scope);
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