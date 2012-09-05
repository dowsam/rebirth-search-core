/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TopChildrenQueryBuilder.java 2012-7-6 14:29:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.commons.xcontent.XContentBuilder;

/**
 * The Class TopChildrenQueryBuilder.
 *
 * @author l.xue.nong
 */
public class TopChildrenQueryBuilder extends BaseQueryBuilder {

	/** The query builder. */
	private final QueryBuilder queryBuilder;

	/** The child type. */
	private String childType;

	/** The scope. */
	private String scope;

	/** The score. */
	private String score;

	/** The boost. */
	private float boost = 1.0f;

	/** The factor. */
	private int factor = -1;

	/** The incremental factor. */
	private int incrementalFactor = -1;

	/**
	 * Instantiates a new top children query builder.
	 *
	 * @param type the type
	 * @param queryBuilder the query builder
	 */
	public TopChildrenQueryBuilder(String type, QueryBuilder queryBuilder) {
		this.childType = type;
		this.queryBuilder = queryBuilder;
	}

	/**
	 * Scope.
	 *
	 * @param scope the scope
	 * @return the top children query builder
	 */
	public TopChildrenQueryBuilder scope(String scope) {
		this.scope = scope;
		return this;
	}

	/**
	 * Score.
	 *
	 * @param score the score
	 * @return the top children query builder
	 */
	public TopChildrenQueryBuilder score(String score) {
		this.score = score;
		return this;
	}

	/**
	 * Factor.
	 *
	 * @param factor the factor
	 * @return the top children query builder
	 */
	public TopChildrenQueryBuilder factor(int factor) {
		this.factor = factor;
		return this;
	}

	/**
	 * Incremental factor.
	 *
	 * @param incrementalFactor the incremental factor
	 * @return the top children query builder
	 */
	public TopChildrenQueryBuilder incrementalFactor(int incrementalFactor) {
		this.incrementalFactor = incrementalFactor;
		return this;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the top children query builder
	 */
	public TopChildrenQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(TopChildrenQueryParser.NAME);
		builder.field("query");
		queryBuilder.toXContent(builder, params);
		builder.field("type", childType);
		if (scope != null) {
			builder.field("_scope", scope);
		}
		if (score != null) {
			builder.field("score", score);
		}
		if (boost != -1) {
			builder.field("boost", boost);
		}
		if (factor != -1) {
			builder.field("factor", factor);
		}
		if (incrementalFactor != -1) {
			builder.field("incremental_factor", incrementalFactor);
		}
		builder.endObject();
	}
}
