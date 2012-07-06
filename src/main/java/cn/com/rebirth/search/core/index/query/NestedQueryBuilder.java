/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NestedQueryBuilder.java 2012-7-6 14:28:52 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class NestedQueryBuilder.
 *
 * @author l.xue.nong
 */
public class NestedQueryBuilder extends BaseQueryBuilder {

	/** The query builder. */
	private final QueryBuilder queryBuilder;

	/** The filter builder. */
	private final FilterBuilder filterBuilder;

	/** The path. */
	private final String path;

	/** The score mode. */
	private String scoreMode;

	/** The boost. */
	private float boost = 1.0f;

	/** The scope. */
	private String scope;

	/**
	 * Instantiates a new nested query builder.
	 *
	 * @param path the path
	 * @param queryBuilder the query builder
	 */
	public NestedQueryBuilder(String path, QueryBuilder queryBuilder) {
		this.path = path;
		this.queryBuilder = queryBuilder;
		this.filterBuilder = null;
	}

	/**
	 * Instantiates a new nested query builder.
	 *
	 * @param path the path
	 * @param filterBuilder the filter builder
	 */
	public NestedQueryBuilder(String path, FilterBuilder filterBuilder) {
		this.path = path;
		this.queryBuilder = null;
		this.filterBuilder = filterBuilder;
	}

	/**
	 * Score mode.
	 *
	 * @param scoreMode the score mode
	 * @return the nested query builder
	 */
	public NestedQueryBuilder scoreMode(String scoreMode) {
		this.scoreMode = scoreMode;
		return this;
	}

	/**
	 * Scope.
	 *
	 * @param scope the scope
	 * @return the nested query builder
	 */
	public NestedQueryBuilder scope(String scope) {
		this.scope = scope;
		return this;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the nested query builder
	 */
	public NestedQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(NestedQueryParser.NAME);
		if (queryBuilder != null) {
			builder.field("query");
			queryBuilder.toXContent(builder, params);
		} else {
			builder.field("filter");
			filterBuilder.toXContent(builder, params);
		}
		builder.field("path", path);
		if (scoreMode != null) {
			builder.field("score_mode", scoreMode);
		}
		if (scope != null) {
			builder.field("_scope", scope);
		}
		if (boost != 1.0f) {
			builder.field("boost", boost);
		}
		builder.endObject();
	}
}
