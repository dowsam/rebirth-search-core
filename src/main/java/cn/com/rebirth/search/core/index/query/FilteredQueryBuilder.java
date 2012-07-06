/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FilteredQueryBuilder.java 2012-7-6 14:30:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class FilteredQueryBuilder.
 *
 * @author l.xue.nong
 */
public class FilteredQueryBuilder extends BaseQueryBuilder {

	/** The query builder. */
	private final QueryBuilder queryBuilder;

	/** The filter builder. */
	private final FilterBuilder filterBuilder;

	/** The boost. */
	private float boost = -1;

	/**
	 * Instantiates a new filtered query builder.
	 *
	 * @param queryBuilder the query builder
	 * @param filterBuilder the filter builder
	 */
	public FilteredQueryBuilder(QueryBuilder queryBuilder, FilterBuilder filterBuilder) {
		this.queryBuilder = queryBuilder;
		this.filterBuilder = filterBuilder;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the filtered query builder
	 */
	public FilteredQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(FilteredQueryParser.NAME);
		builder.field("query");
		queryBuilder.toXContent(builder, params);
		builder.field("filter");
		filterBuilder.toXContent(builder, params);
		if (boost != -1) {
			builder.field("boost", boost);
		}
		builder.endObject();
	}
}
