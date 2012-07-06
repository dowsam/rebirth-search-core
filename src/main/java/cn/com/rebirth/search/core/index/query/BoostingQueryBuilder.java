/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BoostingQueryBuilder.java 2012-3-29 15:02:45 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class BoostingQueryBuilder.
 *
 * @author l.xue.nong
 */
public class BoostingQueryBuilder extends BaseQueryBuilder {

	
	/** The positive query. */
	private QueryBuilder positiveQuery;

	
	/** The negative query. */
	private QueryBuilder negativeQuery;

	
	/** The negative boost. */
	private float negativeBoost = -1;

	
	/** The boost. */
	private float boost = -1;

	
	/**
	 * Instantiates a new boosting query builder.
	 */
	public BoostingQueryBuilder() {

	}

	
	/**
	 * Positive.
	 *
	 * @param positiveQuery the positive query
	 * @return the boosting query builder
	 */
	public BoostingQueryBuilder positive(QueryBuilder positiveQuery) {
		this.positiveQuery = positiveQuery;
		return this;
	}

	
	/**
	 * Negative.
	 *
	 * @param negativeQuery the negative query
	 * @return the boosting query builder
	 */
	public BoostingQueryBuilder negative(QueryBuilder negativeQuery) {
		this.negativeQuery = negativeQuery;
		return this;
	}

	
	/**
	 * Negative boost.
	 *
	 * @param negativeBoost the negative boost
	 * @return the boosting query builder
	 */
	public BoostingQueryBuilder negativeBoost(float negativeBoost) {
		this.negativeBoost = negativeBoost;
		return this;
	}

	
	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the boosting query builder
	 */
	public BoostingQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		if (positiveQuery == null) {
			throw new QueryBuilderException("boosting query requires positive query to be set");
		}
		if (negativeQuery == null) {
			throw new QueryBuilderException("boosting query requires negative query to be set");
		}
		if (negativeBoost == -1) {
			throw new QueryBuilderException("boosting query requires negativeBoost to be set");
		}
		builder.startObject(BoostingQueryParser.NAME);
		builder.field("positive");
		positiveQuery.toXContent(builder, params);
		builder.field("negative");
		negativeQuery.toXContent(builder, params);

		builder.field("negative_boost", negativeBoost);

		if (boost != -1) {
			builder.field("boost", boost);
		}
		builder.endObject();
	}
}