/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryFilterBuilder.java 2012-7-6 14:30:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class QueryFilterBuilder.
 *
 * @author l.xue.nong
 */
public class QueryFilterBuilder extends BaseFilterBuilder {

	/** The query builder. */
	private final QueryBuilder queryBuilder;

	/** The cache. */
	private Boolean cache;

	/** The filter name. */
	private String filterName;

	/**
	 * Instantiates a new query filter builder.
	 *
	 * @param queryBuilder the query builder
	 */
	public QueryFilterBuilder(QueryBuilder queryBuilder) {
		this.queryBuilder = queryBuilder;
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the query filter builder
	 */
	public QueryFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the query filter builder
	 */
	public QueryFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		if (filterName == null && cache == null) {
			builder.field(QueryFilterParser.NAME);
			queryBuilder.toXContent(builder, params);
		} else {
			builder.startObject(FQueryFilterParser.NAME);
			builder.field("query");
			queryBuilder.toXContent(builder, params);
			if (filterName != null) {
				builder.field("_name", filterName);
			}
			if (cache != null) {
				builder.field("_cache", cache);
			}
			builder.endObject();
		}
	}
}
