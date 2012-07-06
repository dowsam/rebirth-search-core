/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SpanNotQueryBuilder.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class SpanNotQueryBuilder.
 *
 * @author l.xue.nong
 */
public class SpanNotQueryBuilder extends BaseQueryBuilder implements SpanQueryBuilder {

	/** The include. */
	private SpanQueryBuilder include;

	/** The exclude. */
	private SpanQueryBuilder exclude;

	/** The boost. */
	private float boost = -1;

	/**
	 * Include.
	 *
	 * @param include the include
	 * @return the span not query builder
	 */
	public SpanNotQueryBuilder include(SpanQueryBuilder include) {
		this.include = include;
		return this;
	}

	/**
	 * Exclude.
	 *
	 * @param exclude the exclude
	 * @return the span not query builder
	 */
	public SpanNotQueryBuilder exclude(SpanQueryBuilder exclude) {
		this.exclude = exclude;
		return this;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the span not query builder
	 */
	public SpanNotQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		if (include == null) {
			throw new QueryBuilderException("Must specify include when using spanNot query");
		}
		if (exclude == null) {
			throw new QueryBuilderException("Must specify exclude when using spanNot query");
		}
		builder.startObject(SpanNotQueryParser.NAME);
		builder.field("include");
		include.toXContent(builder, params);
		builder.field("exclude");
		exclude.toXContent(builder, params);
		if (boost == -1) {
			builder.field("boost", boost);
		}
		builder.endObject();
	}
}
