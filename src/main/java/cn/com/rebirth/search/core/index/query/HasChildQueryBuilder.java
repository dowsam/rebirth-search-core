/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HasChildQueryBuilder.java 2012-7-6 14:29:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class HasChildQueryBuilder.
 *
 * @author l.xue.nong
 */
public class HasChildQueryBuilder extends BaseQueryBuilder {

	/** The query builder. */
	private final QueryBuilder queryBuilder;

	/** The child type. */
	private String childType;

	/** The scope. */
	private String scope;

	/** The boost. */
	private float boost = 1.0f;

	/**
	 * Instantiates a new checks for child query builder.
	 *
	 * @param type the type
	 * @param queryBuilder the query builder
	 */
	public HasChildQueryBuilder(String type, QueryBuilder queryBuilder) {
		this.childType = type;
		this.queryBuilder = queryBuilder;
	}

	/**
	 * Scope.
	 *
	 * @param scope the scope
	 * @return the checks for child query builder
	 */
	public HasChildQueryBuilder scope(String scope) {
		this.scope = scope;
		return this;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the checks for child query builder
	 */
	public HasChildQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(HasChildQueryParser.NAME);
		builder.field("query");
		queryBuilder.toXContent(builder, params);
		builder.field("type", childType);
		if (scope != null) {
			builder.field("_scope", scope);
		}
		if (boost != 1.0f) {
			builder.field("boost", boost);
		}
		builder.endObject();
	}
}
