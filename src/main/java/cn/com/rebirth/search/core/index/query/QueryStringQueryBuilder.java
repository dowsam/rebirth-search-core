/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core QueryStringQueryBuilder.java 2012-3-29 15:02:39 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import static com.google.common.collect.Lists.newArrayList;
import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectFloatHashMap;

import java.io.IOException;
import java.util.List;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class QueryStringQueryBuilder.
 *
 * @author l.xue.nong
 */
public class QueryStringQueryBuilder extends BaseQueryBuilder {

	
	/**
	 * The Enum Operator.
	 *
	 * @author l.xue.nong
	 */
	public static enum Operator {

		
		/** The OR. */
		OR,

		
		/** The AND. */
		AND
	}

	
	/** The query string. */
	private final String queryString;

	
	/** The default field. */
	private String defaultField;

	
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

	
	/** The fields. */
	private List<String> fields;

	
	/** The fields boosts. */
	private TObjectFloatHashMap<String> fieldsBoosts;

	
	/** The use dis max. */
	private Boolean useDisMax;

	
	/** The tie breaker. */
	private float tieBreaker = -1;

	
	/** The rewrite. */
	private String rewrite = null;

	
	/** The minimum should match. */
	private String minimumShouldMatch;

	
	/**
	 * Instantiates a new query string query builder.
	 *
	 * @param queryString the query string
	 */
	public QueryStringQueryBuilder(String queryString) {
		this.queryString = queryString;
	}

	
	/**
	 * Default field.
	 *
	 * @param defaultField the default field
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder defaultField(String defaultField) {
		this.defaultField = defaultField;
		return this;
	}

	
	/**
	 * Field.
	 *
	 * @param field the field
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder field(String field) {
		if (fields == null) {
			fields = newArrayList();
		}
		fields.add(field);
		return this;
	}

	
	/**
	 * Field.
	 *
	 * @param field the field
	 * @param boost the boost
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder field(String field, float boost) {
		if (fields == null) {
			fields = newArrayList();
		}
		fields.add(field);
		if (fieldsBoosts == null) {
			fieldsBoosts = new TObjectFloatHashMap<String>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR,
					-1);
		}
		fieldsBoosts.put(field, boost);
		return this;
	}

	
	/**
	 * Use dis max.
	 *
	 * @param useDisMax the use dis max
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder useDisMax(boolean useDisMax) {
		this.useDisMax = useDisMax;
		return this;
	}

	
	/**
	 * Tie breaker.
	 *
	 * @param tieBreaker the tie breaker
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder tieBreaker(float tieBreaker) {
		this.tieBreaker = tieBreaker;
		return this;
	}

	
	/**
	 * Default operator.
	 *
	 * @param defaultOperator the default operator
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder defaultOperator(Operator defaultOperator) {
		this.defaultOperator = defaultOperator;
		return this;
	}

	
	/**
	 * Analyzer.
	 *
	 * @param analyzer the analyzer
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder analyzer(String analyzer) {
		this.analyzer = analyzer;
		return this;
	}

	
	/**
	 * Auto generate phrase queries.
	 *
	 * @param autoGeneratePhraseQueries the auto generate phrase queries
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder autoGeneratePhraseQueries(boolean autoGeneratePhraseQueries) {
		this.autoGeneratePhraseQueries = autoGeneratePhraseQueries;
		return this;
	}

	
	/**
	 * Allow leading wildcard.
	 *
	 * @param allowLeadingWildcard the allow leading wildcard
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder allowLeadingWildcard(boolean allowLeadingWildcard) {
		this.allowLeadingWildcard = allowLeadingWildcard;
		return this;
	}

	
	/**
	 * Lowercase expanded terms.
	 *
	 * @param lowercaseExpandedTerms the lowercase expanded terms
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder lowercaseExpandedTerms(boolean lowercaseExpandedTerms) {
		this.lowercaseExpandedTerms = lowercaseExpandedTerms;
		return this;
	}

	
	/**
	 * Enable position increments.
	 *
	 * @param enablePositionIncrements the enable position increments
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder enablePositionIncrements(boolean enablePositionIncrements) {
		this.enablePositionIncrements = enablePositionIncrements;
		return this;
	}

	
	/**
	 * Fuzzy min sim.
	 *
	 * @param fuzzyMinSim the fuzzy min sim
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder fuzzyMinSim(float fuzzyMinSim) {
		this.fuzzyMinSim = fuzzyMinSim;
		return this;
	}

	
	/**
	 * Fuzzy prefix length.
	 *
	 * @param fuzzyPrefixLength the fuzzy prefix length
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder fuzzyPrefixLength(int fuzzyPrefixLength) {
		this.fuzzyPrefixLength = fuzzyPrefixLength;
		return this;
	}

	
	/**
	 * Phrase slop.
	 *
	 * @param phraseSlop the phrase slop
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder phraseSlop(int phraseSlop) {
		this.phraseSlop = phraseSlop;
		return this;
	}

	
	/**
	 * Analyze wildcard.
	 *
	 * @param analyzeWildcard the analyze wildcard
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder analyzeWildcard(boolean analyzeWildcard) {
		this.analyzeWildcard = analyzeWildcard;
		return this;
	}

	
	/**
	 * Rewrite.
	 *
	 * @param rewrite the rewrite
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder rewrite(String rewrite) {
		this.rewrite = rewrite;
		return this;
	}

	
	/**
	 * Minimum should match.
	 *
	 * @param minimumShouldMatch the minimum should match
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder minimumShouldMatch(String minimumShouldMatch) {
		this.minimumShouldMatch = minimumShouldMatch;
		return this;
	}

	
	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the query string query builder
	 */
	public QueryStringQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(QueryStringQueryParser.NAME);
		builder.field("query", queryString);
		if (defaultField != null) {
			builder.field("default_field", defaultField);
		}
		if (fields != null) {
			builder.startArray("fields");
			for (String field : fields) {
				float boost = -1;
				if (fieldsBoosts != null) {
					boost = fieldsBoosts.get(field);
				}
				if (boost != -1) {
					field += "^" + boost;
				}
				builder.value(field);
			}
			builder.endArray();
		}
		if (useDisMax != null) {
			builder.field("use_dis_max", useDisMax);
		}
		if (tieBreaker != -1) {
			builder.field("tie_breaker", tieBreaker);
		}
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
}
