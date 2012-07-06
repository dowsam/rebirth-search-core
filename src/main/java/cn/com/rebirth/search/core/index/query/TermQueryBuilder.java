/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermQueryBuilder.java 2012-7-6 14:29:16 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class TermQueryBuilder.
 *
 * @author l.xue.nong
 */
public class TermQueryBuilder extends BaseQueryBuilder {

	/** The name. */
	private final String name;

	/** The value. */
	private final Object value;

	/** The boost. */
	private float boost = -1;

	/**
	 * Instantiates a new term query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public TermQueryBuilder(String name, String value) {
		this(name, (Object) value);
	}

	/**
	 * Instantiates a new term query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public TermQueryBuilder(String name, int value) {
		this(name, (Object) value);
	}

	/**
	 * Instantiates a new term query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public TermQueryBuilder(String name, long value) {
		this(name, (Object) value);
	}

	/**
	 * Instantiates a new term query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public TermQueryBuilder(String name, float value) {
		this(name, (Object) value);
	}

	/**
	 * Instantiates a new term query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public TermQueryBuilder(String name, double value) {
		this(name, (Object) value);
	}

	/**
	 * Instantiates a new term query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public TermQueryBuilder(String name, boolean value) {
		this(name, (Object) value);
	}

	/**
	 * Instantiates a new term query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public TermQueryBuilder(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the term query builder
	 */
	public TermQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(TermQueryParser.NAME);
		if (boost == -1) {
			builder.field(name, value);
		} else {
			builder.startObject(name);
			builder.field("value", value);
			builder.field("boost", boost);
			builder.endObject();
		}
		builder.endObject();
	}
}
