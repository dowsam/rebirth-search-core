/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesQueryBuilder.java 2012-7-6 14:30:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.commons.xcontent.XContentBuilder;

/**
 * The Class IndicesQueryBuilder.
 *
 * @author l.xue.nong
 */
public class IndicesQueryBuilder extends BaseQueryBuilder {

	/** The query builder. */
	private final QueryBuilder queryBuilder;

	/** The indices. */
	private final String[] indices;

	/** The s no match query. */
	private String sNoMatchQuery;

	/** The no match query. */
	private QueryBuilder noMatchQuery;

	/**
	 * Instantiates a new indices query builder.
	 *
	 * @param queryBuilder the query builder
	 * @param indices the indices
	 */
	public IndicesQueryBuilder(QueryBuilder queryBuilder, String... indices) {
		this.queryBuilder = queryBuilder;
		this.indices = indices;
	}

	/**
	 * No match query.
	 *
	 * @param type the type
	 * @return the indices query builder
	 */
	public IndicesQueryBuilder noMatchQuery(String type) {
		this.sNoMatchQuery = type;
		return this;
	}

	/**
	 * No match query.
	 *
	 * @param noMatchQuery the no match query
	 * @return the indices query builder
	 */
	public IndicesQueryBuilder noMatchQuery(QueryBuilder noMatchQuery) {
		this.noMatchQuery = noMatchQuery;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(IndicesQueryParser.NAME);
		builder.field("query");
		queryBuilder.toXContent(builder, params);
		builder.field("indices", indices);
		if (noMatchQuery != null) {
			builder.field("no_match_query");
			noMatchQuery.toXContent(builder, params);
		} else if (sNoMatchQuery != null) {
			builder.field("no_match_query", sNoMatchQuery);
		}
		builder.endObject();
	}
}