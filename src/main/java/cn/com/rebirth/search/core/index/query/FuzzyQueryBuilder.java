/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FuzzyQueryBuilder.java 2012-3-29 15:00:53 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class FuzzyQueryBuilder.
 *
 * @author l.xue.nong
 */
public class FuzzyQueryBuilder extends BaseQueryBuilder {

	
	/** The name. */
	private final String name;

	
	/** The value. */
	private final Object value;

	
	/** The boost. */
	private float boost = -1;

	
	/** The min similarity. */
	private String minSimilarity;

	
	/** The prefix length. */
	private Integer prefixLength;

	
	/**
	 * Instantiates a new fuzzy query builder.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public FuzzyQueryBuilder(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	
	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the fuzzy query builder
	 */
	public FuzzyQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	
	/**
	 * Min similarity.
	 *
	 * @param defaultMinSimilarity the default min similarity
	 * @return the fuzzy query builder
	 */
	public FuzzyQueryBuilder minSimilarity(float defaultMinSimilarity) {
		this.minSimilarity = Float.toString(defaultMinSimilarity);
		return this;
	}

	
	/**
	 * Min similarity.
	 *
	 * @param defaultMinSimilarity the default min similarity
	 * @return the fuzzy query builder
	 */
	public FuzzyQueryBuilder minSimilarity(String defaultMinSimilarity) {
		this.minSimilarity = defaultMinSimilarity;
		return this;
	}

	
	/**
	 * Prefix length.
	 *
	 * @param prefixLength the prefix length
	 * @return the fuzzy query builder
	 */
	public FuzzyQueryBuilder prefixLength(int prefixLength) {
		this.prefixLength = prefixLength;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(FuzzyQueryParser.NAME);
		if (boost == -1 && minSimilarity == null && prefixLength == null) {
			builder.field(name, value);
		} else {
			builder.startObject(name);
			builder.field("value", value);
			if (boost != -1) {
				builder.field("boost", boost);
			}
			if (minSimilarity != null) {
				builder.field("min_similarity", minSimilarity);
			}
			if (prefixLength != null) {
				builder.field("prefix_length", prefixLength);
			}
			builder.endObject();
		}
		builder.endObject();
	}
}