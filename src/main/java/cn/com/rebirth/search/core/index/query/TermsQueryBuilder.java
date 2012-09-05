/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermsQueryBuilder.java 2012-7-6 14:29:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.commons.xcontent.XContentBuilder;

/**
 * The Class TermsQueryBuilder.
 *
 * @author l.xue.nong
 */
public class TermsQueryBuilder extends BaseQueryBuilder {

	/** The name. */
	private final String name;

	/** The values. */
	private final Object[] values;

	/** The minimum match. */
	private int minimumMatch = -1;

	/** The disable coord. */
	private Boolean disableCoord;

	/** The boost. */
	private float boost = -1;

	/**
	 * Instantiates a new terms query builder.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public TermsQueryBuilder(String name, String... values) {
		this(name, (Object[]) values);
	}

	/**
	 * Instantiates a new terms query builder.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public TermsQueryBuilder(String name, int... values) {
		this.name = name;
		this.values = new Integer[values.length];
		for (int i = 0; i < values.length; i++) {
			this.values[i] = values[i];
		}
	}

	/**
	 * Instantiates a new terms query builder.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public TermsQueryBuilder(String name, long... values) {
		this.name = name;
		this.values = new Long[values.length];
		for (int i = 0; i < values.length; i++) {
			this.values[i] = values[i];
		}
	}

	/**
	 * Instantiates a new terms query builder.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public TermsQueryBuilder(String name, float... values) {
		this.name = name;
		this.values = new Float[values.length];
		for (int i = 0; i < values.length; i++) {
			this.values[i] = values[i];
		}
	}

	/**
	 * Instantiates a new terms query builder.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public TermsQueryBuilder(String name, double... values) {
		this.name = name;
		this.values = new Double[values.length];
		for (int i = 0; i < values.length; i++) {
			this.values[i] = values[i];
		}
	}

	/**
	 * Instantiates a new terms query builder.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public TermsQueryBuilder(String name, Object... values) {
		this.name = name;
		this.values = values;
	}

	/**
	 * Minimum match.
	 *
	 * @param minimumMatch the minimum match
	 * @return the terms query builder
	 */
	public TermsQueryBuilder minimumMatch(int minimumMatch) {
		this.minimumMatch = minimumMatch;
		return this;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the terms query builder
	 */
	public TermsQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/**
	 * Disable coord.
	 *
	 * @param disableCoord the disable coord
	 * @return the terms query builder
	 */
	public TermsQueryBuilder disableCoord(boolean disableCoord) {
		this.disableCoord = disableCoord;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(TermsQueryParser.NAME);
		builder.startArray(name);
		for (Object value : values) {
			builder.value(value);
		}
		builder.endArray();

		if (minimumMatch != -1) {
			builder.field("minimum_match", minimumMatch);
		}
		if (disableCoord != null) {
			builder.field("disable_coord", disableCoord);
		}
		if (boost != -1) {
			builder.field("boost", boost);
		}

		builder.endObject();
	}
}
