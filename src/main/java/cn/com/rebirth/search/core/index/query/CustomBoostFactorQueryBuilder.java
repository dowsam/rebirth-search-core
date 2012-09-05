/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CustomBoostFactorQueryBuilder.java 2012-7-6 14:28:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.commons.xcontent.XContentBuilder;

/**
 * The Class CustomBoostFactorQueryBuilder.
 *
 * @author l.xue.nong
 */
public class CustomBoostFactorQueryBuilder extends BaseQueryBuilder {

	/** The query builder. */
	private final QueryBuilder queryBuilder;

	/** The boost factor. */
	private float boostFactor = -1;

	/**
	 * Instantiates a new custom boost factor query builder.
	 *
	 * @param queryBuilder the query builder
	 */
	public CustomBoostFactorQueryBuilder(QueryBuilder queryBuilder) {
		this.queryBuilder = queryBuilder;
	}

	/**
	 * Boost factor.
	 *
	 * @param boost the boost
	 * @return the custom boost factor query builder
	 */
	public CustomBoostFactorQueryBuilder boostFactor(float boost) {
		this.boostFactor = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(CustomBoostFactorQueryParser.NAME);
		builder.field("query");
		queryBuilder.toXContent(builder, params);
		if (boostFactor != -1) {
			builder.field("boost_factor", boostFactor);
		}
		builder.endObject();
	}
}