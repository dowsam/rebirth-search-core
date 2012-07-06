/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FieldMaskingSpanQueryBuilder.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class FieldMaskingSpanQueryBuilder.
 *
 * @author l.xue.nong
 */
public class FieldMaskingSpanQueryBuilder extends BaseQueryBuilder implements SpanQueryBuilder {

	/** The query builder. */
	private final SpanQueryBuilder queryBuilder;

	/** The field. */
	private final String field;

	/** The boost. */
	private float boost = -1;

	/**
	 * Instantiates a new field masking span query builder.
	 *
	 * @param queryBuilder the query builder
	 * @param field the field
	 */
	public FieldMaskingSpanQueryBuilder(SpanQueryBuilder queryBuilder, String field) {
		this.queryBuilder = queryBuilder;
		this.field = field;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the field masking span query builder
	 */
	public FieldMaskingSpanQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(FieldMaskingSpanQueryParser.NAME);
		builder.field("query");
		queryBuilder.toXContent(builder, params);
		builder.field("field", field);
		if (boost != -1) {
			builder.field("boost", boost);
		}
		builder.endObject();
	}
}
