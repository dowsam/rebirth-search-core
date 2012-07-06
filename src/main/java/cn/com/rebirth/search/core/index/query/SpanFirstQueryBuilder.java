/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SpanFirstQueryBuilder.java 2012-7-6 14:30:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class SpanFirstQueryBuilder.
 *
 * @author l.xue.nong
 */
public class SpanFirstQueryBuilder extends BaseQueryBuilder implements SpanQueryBuilder {

	/** The match builder. */
	private final SpanQueryBuilder matchBuilder;

	/** The end. */
	private final int end;

	/** The boost. */
	private float boost = -1;

	/**
	 * Instantiates a new span first query builder.
	 *
	 * @param matchBuilder the match builder
	 * @param end the end
	 */
	public SpanFirstQueryBuilder(SpanQueryBuilder matchBuilder, int end) {
		this.matchBuilder = matchBuilder;
		this.end = end;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the span first query builder
	 */
	public SpanFirstQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(SpanFirstQueryParser.NAME);
		builder.field("match");
		matchBuilder.toXContent(builder, params);
		builder.field("end", end);
		if (boost != -1) {
			builder.field("boost", boost);
		}
		builder.endObject();
	}
}
