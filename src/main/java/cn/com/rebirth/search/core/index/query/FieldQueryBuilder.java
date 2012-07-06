/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FieldQueryBuilder.java 2012-7-6 14:30:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class FieldQueryBuilder.
 *
 * @author l.xue.nong
 */
public class FieldQueryBuilder extends BaseQueryBuilder {

	/**
	 * The Enum Operator.
	 *
	 * @author l.xue.nong
	 */
	public static enum Operator {

		/** The or. */
		OR,

		/** The and. */
		AND
	}

	/** The name. */
	private final String name;

	/** The query. */
	private final Object query;

	/** The default operator. */
	private Operator defaultOperator;

	/** The analyzer. */
	private String analyzer;

	/** The auto generate phrase queries. */
	private Boolean autoGeneratePhraseQueries;

	/** The allow leading wildcard. */
	private Boolean allowLeadingWildcard;

	/** The lowercase expanded terms. */
	private Boolean lowercaseExpandedTerms;

	/** The enable position increments. */
	private Boolean enablePositionIncrements;

	/** The analyze wildcard. */
	private Boolean analyzeWildcard;

	/** The fuzzy min sim. */
	private float fuzzyMinSim = -1;

	/** The boost. */
	private float boost = -1;

	/** The fuzzy prefix length. */
	private int fuzzyPrefixLength = -1;

	/** The phrase slop. */
	private int phraseSlop = -1;

	/** The extra set. */
	private boolean extraSet = false;

	/** The rewrite. */
	private String rewrite;

	/** The minimum should match. */
	private String minimumShouldMatch;

	/**
	 * Instantiates a new field query builder.
	 *
	 * @param name the name
	 * @param query the query
	 */
	public FieldQueryBuilder(String name, String query) {
		this(name, (Object) query);
	}

	/**
	 * Instantiates a new field query builder.
	 *
	 * @param name the name
	 * @param query the query
	 */
	public FieldQueryBuilder(String name, int query) {
		this(name, (Object) query);
	}

	/**
	 * Instantiates a new field query builder.
	 *
	 * @param name the name
	 * @param query the query
	 */
	public FieldQueryBuilder(String name, long query) {
		this(name, (Object) query);
	}

	/**
	 * Instantiates a new field query builder.
	 *
	 * @param name the name
	 * @param query the query
	 */
	public FieldQueryBuilder(String name, float query) {
		this(name, (Object) query);
	}

	/**
	 * Instantiates a new field query builder.
	 *
	 * @param name the name
	 * @param query the query
	 */
	public FieldQueryBuilder(String name, double query) {
		this(name, (Object) query);
	}

	/**
	 * Instantiates a new field query builder.
	 *
	 * @param name the name
	 * @param query the query
	 */
	public FieldQueryBuilder(String name, boolean query) {
		this(name, (Object) query);
	}

	/**
	 * Instantiates a new field query builder.
	 *
	 * @param name the name
	 * @param query the query
	 */
	public FieldQueryBuilder(String name, Object query) {
		this.name = name;
		this.query = query;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the field query builder
	 */
	public FieldQueryBuilder boost(float boost) {
		this.boost = boost;
		extraSet = true;
		return this;
	}

	/**
	 * Default operator.
	 *
	 * @param defaultOperator the default operator
	 * @return the field query builder
	 */
	public FieldQueryBuilder defaultOperator(Operator defaultOperator) {
		this.defaultOperator = defaultOperator;
		extraSet = true;
		return this;
	}

	/**
	 * Analyzer.
	 *
	 * @param analyzer the analyzer
	 * @return the field query builder
	 */
	public FieldQueryBuilder analyzer(String analyzer) {
		this.analyzer = analyzer;
		extraSet = true;
		return this;
	}

	/**
	 * Auto generate phrase queries.
	 *
	 * @param autoGeneratePhraseQueries the auto generate phrase queries
	 */
	public void autoGeneratePhraseQueries(boolean autoGeneratePhraseQueries) {
		this.autoGeneratePhraseQueries = autoGeneratePhraseQueries;
	}

	/**
	 * Allow leading wildcard.
	 *
	 * @param allowLeadingWildcard the allow leading wildcard
	 * @return the field query builder
	 */
	public FieldQueryBuilder allowLeadingWildcard(boolean allowLeadingWildcard) {
		this.allowLeadingWildcard = allowLeadingWildcard;
		extraSet = true;
		return this;
	}

	/**
	 * Lowercase expanded terms.
	 *
	 * @param lowercaseExpandedTerms the lowercase expanded terms
	 * @return the field query builder
	 */
	public FieldQueryBuilder lowercaseExpandedTerms(boolean lowercaseExpandedTerms) {
		this.lowercaseExpandedTerms = lowercaseExpandedTerms;
		extraSet = true;
		return this;
	}

	/**
	 * Enable position increments.
	 *
	 * @param enablePositionIncrements the enable position increments
	 * @return the field query builder
	 */
	public FieldQueryBuilder enablePositionIncrements(boolean enablePositionIncrements) {
		this.enablePositionIncrements = enablePositionIncrements;
		extraSet = true;
		return this;
	}

	/**
	 * Fuzzy min sim.
	 *
	 * @param fuzzyMinSim the fuzzy min sim
	 * @return the field query builder
	 */
	public FieldQueryBuilder fuzzyMinSim(float fuzzyMinSim) {
		this.fuzzyMinSim = fuzzyMinSim;
		extraSet = true;
		return this;
	}

	/**
	 * Fuzzy prefix length.
	 *
	 * @param fuzzyPrefixLength the fuzzy prefix length
	 * @return the field query builder
	 */
	public FieldQueryBuilder fuzzyPrefixLength(int fuzzyPrefixLength) {
		this.fuzzyPrefixLength = fuzzyPrefixLength;
		extraSet = true;
		return this;
	}

	/**
	 * Phrase slop.
	 *
	 * @param phraseSlop the phrase slop
	 * @return the field query builder
	 */
	public FieldQueryBuilder phraseSlop(int phraseSlop) {
		this.phraseSlop = phraseSlop;
		extraSet = true;
		return this;
	}

	/**
	 * Analyze wildcard.
	 *
	 * @param analyzeWildcard the analyze wildcard
	 * @return the field query builder
	 */
	public FieldQueryBuilder analyzeWildcard(boolean analyzeWildcard) {
		this.analyzeWildcard = analyzeWildcard;
		extraSet = true;
		return this;
	}

	/**
	 * Rewrite.
	 *
	 * @param rewrite the rewrite
	 * @return the field query builder
	 */
	public FieldQueryBuilder rewrite(String rewrite) {
		this.rewrite = rewrite;
		extraSet = true;
		return this;
	}

	/**
	 * Minimum should match.
	 *
	 * @param minimumShouldMatch the minimum should match
	 * @return the field query builder
	 */
	public FieldQueryBuilder minimumShouldMatch(String minimumShouldMatch) {
		this.minimumShouldMatch = minimumShouldMatch;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(FieldQueryParser.NAME);
		if (!extraSet) {
			builder.field(name, query);
		} else {
			builder.startObject(name);
			builder.field("query", query);
			if (defaultOperator != null) {
				builder.field("default_operator", defaultOperator.name().toLowerCase());
			}
			if (analyzer != null) {
				builder.field("analyzer", analyzer);
			}
			if (autoGeneratePhraseQueries != null) {
				builder.field("auto_generate_phrase_queries", autoGeneratePhraseQueries);
			}
			if (allowLeadingWildcard != null) {
				builder.field("allow_leading_wildcard", allowLeadingWildcard);
			}
			if (lowercaseExpandedTerms != null) {
				builder.field("lowercase_expanded_terms", lowercaseExpandedTerms);
			}
			if (enablePositionIncrements != null) {
				builder.field("enable_position_increments", enablePositionIncrements);
			}
			if (fuzzyMinSim != -1) {
				builder.field("fuzzy_min_sim", fuzzyMinSim);
			}
			if (boost != -1) {
				builder.field("boost", boost);
			}
			if (fuzzyPrefixLength != -1) {
				builder.field("fuzzy_prefix_length", fuzzyPrefixLength);
			}
			if (phraseSlop != -1) {
				builder.field("phrase_slop", phraseSlop);
			}
			if (analyzeWildcard != null) {
				builder.field("analyze_wildcard", analyzeWildcard);
			}
			if (rewrite != null) {
				builder.field("rewrite", rewrite);
			}
			if (minimumShouldMatch != null) {
				builder.field("minimum_should_match", minimumShouldMatch);
			}
			builder.endObject();
		}
		builder.endObject();
	}
}