/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FuzzyLikeThisQueryBuilder.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.commons.xcontent.XContentBuilder;

/**
 * The Class FuzzyLikeThisQueryBuilder.
 *
 * @author l.xue.nong
 */
public class FuzzyLikeThisQueryBuilder extends BaseQueryBuilder {

	/** The fields. */
	private final String[] fields;

	/** The boost. */
	private Float boost;

	/** The like text. */
	private String likeText = null;

	/** The min similarity. */
	private Float minSimilarity;

	/** The prefix length. */
	private Integer prefixLength;

	/** The max query terms. */
	private Integer maxQueryTerms;

	/** The ignore tf. */
	private Boolean ignoreTF;

	/** The analyzer. */
	private String analyzer;

	/**
	 * Instantiates a new fuzzy like this query builder.
	 */
	public FuzzyLikeThisQueryBuilder() {
		this.fields = null;
	}

	/**
	 * Instantiates a new fuzzy like this query builder.
	 *
	 * @param fields the fields
	 */
	public FuzzyLikeThisQueryBuilder(String... fields) {
		this.fields = fields;
	}

	/**
	 * Like text.
	 *
	 * @param likeText the like text
	 * @return the fuzzy like this query builder
	 */
	public FuzzyLikeThisQueryBuilder likeText(String likeText) {
		this.likeText = likeText;
		return this;
	}

	/**
	 * Min similarity.
	 *
	 * @param minSimilarity the min similarity
	 * @return the fuzzy like this query builder
	 */
	public FuzzyLikeThisQueryBuilder minSimilarity(float minSimilarity) {
		this.minSimilarity = minSimilarity;
		return this;
	}

	/**
	 * Prefix length.
	 *
	 * @param prefixLength the prefix length
	 * @return the fuzzy like this query builder
	 */
	public FuzzyLikeThisQueryBuilder prefixLength(int prefixLength) {
		this.prefixLength = prefixLength;
		return this;
	}

	/**
	 * Max query terms.
	 *
	 * @param maxQueryTerms the max query terms
	 * @return the fuzzy like this query builder
	 */
	public FuzzyLikeThisQueryBuilder maxQueryTerms(int maxQueryTerms) {
		this.maxQueryTerms = maxQueryTerms;
		return this;
	}

	/**
	 * Ignore tf.
	 *
	 * @param ignoreTF the ignore tf
	 * @return the fuzzy like this query builder
	 */
	public FuzzyLikeThisQueryBuilder ignoreTF(boolean ignoreTF) {
		this.ignoreTF = ignoreTF;
		return this;
	}

	/**
	 * Analyzer.
	 *
	 * @param analyzer the analyzer
	 * @return the fuzzy like this query builder
	 */
	public FuzzyLikeThisQueryBuilder analyzer(String analyzer) {
		this.analyzer = analyzer;
		return this;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the fuzzy like this query builder
	 */
	public FuzzyLikeThisQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(FuzzyLikeThisQueryParser.NAME);
		if (fields != null) {
			builder.startArray("fields");
			for (String field : fields) {
				builder.value(field);
			}
			builder.endArray();
		}
		if (likeText == null) {
			throw new QueryBuilderException("fuzzyLikeThis requires 'likeText' to be provided");
		}
		builder.field("like_text", likeText);
		if (maxQueryTerms != null) {
			builder.field("max_query_terms", maxQueryTerms);
		}
		if (minSimilarity != null) {
			builder.field("min_similarity", minSimilarity);
		}
		if (prefixLength != null) {
			builder.field("prefix_length", prefixLength);
		}
		if (ignoreTF != null) {
			builder.field("ignore_tf", ignoreTF);
		}
		if (boost != null) {
			builder.field("boost", boost);
		}
		if (analyzer != null) {
			builder.field("analyzer", analyzer);
		}
		builder.endObject();
	}
}