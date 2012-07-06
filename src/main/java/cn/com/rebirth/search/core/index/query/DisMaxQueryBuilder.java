/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DisMaxQueryBuilder.java 2012-3-29 15:01:17 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.ArrayList;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class DisMaxQueryBuilder.
 *
 * @author l.xue.nong
 */
public class DisMaxQueryBuilder extends BaseQueryBuilder {

	
	/** The queries. */
	private ArrayList<QueryBuilder> queries = newArrayList();

	
	/** The boost. */
	private float boost = -1;

	
	/** The tie breaker. */
	private float tieBreaker = -1;

	
	/**
	 * Adds the.
	 *
	 * @param queryBuilder the query builder
	 * @return the dis max query builder
	 */
	public DisMaxQueryBuilder add(QueryBuilder queryBuilder) {
		queries.add(queryBuilder);
		return this;
	}

	
	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the dis max query builder
	 */
	public DisMaxQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	
	/**
	 * Tie breaker.
	 *
	 * @param tieBreaker the tie breaker
	 * @return the dis max query builder
	 */
	public DisMaxQueryBuilder tieBreaker(float tieBreaker) {
		this.tieBreaker = tieBreaker;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(DisMaxQueryParser.NAME);
		if (tieBreaker != -1) {
			builder.field("tie_breaker", tieBreaker);
		}
		if (boost != -1) {
			builder.field("boost", boost);
		}
		builder.startArray("queries");
		for (QueryBuilder queryBuilder : queries) {
			queryBuilder.toXContent(builder, params);
		}
		builder.endArray();
		builder.endObject();
	}
}