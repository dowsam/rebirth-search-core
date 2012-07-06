/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BoolQueryBuilder.java 2012-7-6 14:28:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class BoolQueryBuilder.
 *
 * @author l.xue.nong
 */
public class BoolQueryBuilder extends BaseQueryBuilder {

	/** The must clauses. */
	private ArrayList<QueryBuilder> mustClauses = new ArrayList<QueryBuilder>();

	/** The must not clauses. */
	private ArrayList<QueryBuilder> mustNotClauses = new ArrayList<QueryBuilder>();

	/** The should clauses. */
	private ArrayList<QueryBuilder> shouldClauses = new ArrayList<QueryBuilder>();

	/** The boost. */
	private float boost = -1;

	/** The disable coord. */
	private Boolean disableCoord;

	/** The minimum number should match. */
	private int minimumNumberShouldMatch = -1;

	/**
	 * Must.
	 *
	 * @param queryBuilder the query builder
	 * @return the bool query builder
	 */
	public BoolQueryBuilder must(QueryBuilder queryBuilder) {
		mustClauses.add(queryBuilder);
		return this;
	}

	/**
	 * Must not.
	 *
	 * @param queryBuilder the query builder
	 * @return the bool query builder
	 */
	public BoolQueryBuilder mustNot(QueryBuilder queryBuilder) {
		mustNotClauses.add(queryBuilder);
		return this;
	}

	/**
	 * Should.
	 *
	 * @param queryBuilder the query builder
	 * @return the bool query builder
	 */
	public BoolQueryBuilder should(QueryBuilder queryBuilder) {
		shouldClauses.add(queryBuilder);
		return this;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the bool query builder
	 */
	public BoolQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/**
	 * Disable coord.
	 *
	 * @param disableCoord the disable coord
	 * @return the bool query builder
	 */
	public BoolQueryBuilder disableCoord(boolean disableCoord) {
		this.disableCoord = disableCoord;
		return this;
	}

	/**
	 * Minimum number should match.
	 *
	 * @param minimumNumberShouldMatch the minimum number should match
	 * @return the bool query builder
	 */
	public BoolQueryBuilder minimumNumberShouldMatch(int minimumNumberShouldMatch) {
		this.minimumNumberShouldMatch = minimumNumberShouldMatch;
		return this;
	}

	/**
	 * Checks for clauses.
	 *
	 * @return true, if successful
	 */
	public boolean hasClauses() {
		return !mustClauses.isEmpty() || !mustNotClauses.isEmpty() || !shouldClauses.isEmpty();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject("bool");
		doXArrayContent("must", mustClauses, builder, params);
		doXArrayContent("must_not", mustNotClauses, builder, params);
		doXArrayContent("should", shouldClauses, builder, params);
		if (boost != -1) {
			builder.field("boost", boost);
		}
		if (disableCoord != null) {
			builder.field("disable_coord", disableCoord);
		}
		if (minimumNumberShouldMatch != -1) {
			builder.field("minimum_number_should_match", minimumNumberShouldMatch);
		}
		builder.endObject();
	}

	/**
	 * Do x array content.
	 *
	 * @param field the field
	 * @param clauses the clauses
	 * @param builder the builder
	 * @param params the params
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void doXArrayContent(String field, List<QueryBuilder> clauses, XContentBuilder builder, Params params)
			throws IOException {
		if (clauses.isEmpty()) {
			return;
		}
		if (clauses.size() == 1) {
			builder.field(field);
			clauses.get(0).toXContent(builder, params);
		} else {
			builder.startArray(field);
			for (QueryBuilder clause : clauses) {
				clause.toXContent(builder, params);
			}
			builder.endArray();
		}
	}

}
