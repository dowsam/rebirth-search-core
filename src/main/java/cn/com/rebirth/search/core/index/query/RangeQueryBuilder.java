/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RangeQueryBuilder.java 2012-3-29 15:01:04 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class RangeQueryBuilder.
 *
 * @author l.xue.nong
 */
public class RangeQueryBuilder extends BaseQueryBuilder {

	
	/** The name. */
	private final String name;

	
	/** The from. */
	private Object from;

	
	/** The to. */
	private Object to;

	
	/** The include lower. */
	private boolean includeLower = true;

	
	/** The include upper. */
	private boolean includeUpper = true;

	
	/** The boost. */
	private float boost = -1;

	
	/**
	 * Instantiates a new range query builder.
	 *
	 * @param name the name
	 */
	public RangeQueryBuilder(String name) {
		this.name = name;
	}

	
	/**
	 * From.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder from(Object from) {
		this.from = from;
		return this;
	}

	
	/**
	 * From.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder from(String from) {
		this.from = from;
		return this;
	}

	
	/**
	 * From.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder from(int from) {
		this.from = from;
		return this;
	}

	
	/**
	 * From.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder from(long from) {
		this.from = from;
		return this;
	}

	
	/**
	 * From.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder from(float from) {
		this.from = from;
		return this;
	}

	
	/**
	 * From.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder from(double from) {
		this.from = from;
		return this;
	}

	
	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder gt(String from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	
	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder gt(Object from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	
	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder gt(int from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	
	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder gt(long from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	
	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder gt(float from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	
	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder gt(double from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	
	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder gte(String from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	
	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder gte(Object from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	
	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder gte(int from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	
	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder gte(long from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	
	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder gte(float from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	
	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the range query builder
	 */
	public RangeQueryBuilder gte(double from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	
	/**
	 * To.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder to(Object to) {
		this.to = to;
		return this;
	}

	
	/**
	 * To.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder to(String to) {
		this.to = to;
		return this;
	}

	
	/**
	 * To.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder to(int to) {
		this.to = to;
		return this;
	}

	
	/**
	 * To.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder to(long to) {
		this.to = to;
		return this;
	}

	
	/**
	 * To.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder to(float to) {
		this.to = to;
		return this;
	}

	
	/**
	 * To.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder to(double to) {
		this.to = to;
		return this;
	}

	
	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder lt(String to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	
	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder lt(Object to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	
	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder lt(int to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	
	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder lt(long to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	
	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder lt(float to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	
	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder lt(double to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	
	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder lte(String to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	
	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder lte(Object to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	
	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder lte(int to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	
	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder lte(long to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	
	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder lte(float to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	
	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the range query builder
	 */
	public RangeQueryBuilder lte(double to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	
	/**
	 * Include lower.
	 *
	 * @param includeLower the include lower
	 * @return the range query builder
	 */
	public RangeQueryBuilder includeLower(boolean includeLower) {
		this.includeLower = includeLower;
		return this;
	}

	
	/**
	 * Include upper.
	 *
	 * @param includeUpper the include upper
	 * @return the range query builder
	 */
	public RangeQueryBuilder includeUpper(boolean includeUpper) {
		this.includeUpper = includeUpper;
		return this;
	}

	
	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the range query builder
	 */
	public RangeQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(RangeQueryParser.NAME);
		builder.startObject(name);
		builder.field("from", from);
		builder.field("to", to);
		builder.field("include_lower", includeLower);
		builder.field("include_upper", includeUpper);
		if (boost != -1) {
			builder.field("boost", boost);
		}
		builder.endObject();
		builder.endObject();
	}
}
