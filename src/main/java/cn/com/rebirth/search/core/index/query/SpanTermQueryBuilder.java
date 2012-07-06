/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SpanTermQueryBuilder.java 2012-3-29 15:01:09 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class SpanTermQueryBuilder.
 *
 * @author l.xue.nong
 */
public class SpanTermQueryBuilder extends BaseQueryBuilder implements SpanQueryBuilder {

	
	/** The name. */
	private final String name;

	
	/** The value. */
	private final Object value;

	
	/** The boost. */
	private float boost = -1;

	
	/**
	 * Instantiates a new span term query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public SpanTermQueryBuilder(String name, String value) {
		this(name, (Object) value);
	}

	
	/**
	 * Instantiates a new span term query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public SpanTermQueryBuilder(String name, int value) {
		this(name, (Object) value);
	}

	
	/**
	 * Instantiates a new span term query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public SpanTermQueryBuilder(String name, long value) {
		this(name, (Object) value);
	}

	
	/**
	 * Instantiates a new span term query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public SpanTermQueryBuilder(String name, float value) {
		this(name, (Object) value);
	}

	
	/**
	 * Instantiates a new span term query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public SpanTermQueryBuilder(String name, double value) {
		this(name, (Object) value);
	}

	
	/**
	 * Instantiates a new span term query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	private SpanTermQueryBuilder(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	
	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the span term query builder
	 */
	public SpanTermQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(SpanTermQueryParser.NAME);
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