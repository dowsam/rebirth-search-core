/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SpanOrQueryBuilder.java 2012-7-6 14:28:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.ArrayList;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class SpanOrQueryBuilder.
 *
 * @author l.xue.nong
 */
public class SpanOrQueryBuilder extends BaseQueryBuilder implements SpanQueryBuilder {

	/** The clauses. */
	private ArrayList<SpanQueryBuilder> clauses = new ArrayList<SpanQueryBuilder>();

	/** The boost. */
	private float boost = -1;

	/**
	 * Clause.
	 *
	 * @param clause the clause
	 * @return the span or query builder
	 */
	public SpanOrQueryBuilder clause(SpanQueryBuilder clause) {
		clauses.add(clause);
		return this;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the span or query builder
	 */
	public SpanOrQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		if (clauses.isEmpty()) {
			throw new QueryBuilderException("Must have at least one clause when building a spanOr query");
		}
		builder.startObject(SpanOrQueryParser.NAME);
		builder.startArray("clauses");
		for (SpanQueryBuilder clause : clauses) {
			clause.toXContent(builder, params);
		}
		builder.endArray();
		if (boost != -1) {
			builder.field("boost", boost);
		}
		builder.endObject();
	}
}