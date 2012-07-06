/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryParserSettings.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package org.apache.lucene.queryParser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiTermQuery;

/**
 * The Class QueryParserSettings.
 *
 * @author l.xue.nong
 */
public class QueryParserSettings {

	/** The Constant DEFAULT_ALLOW_LEADING_WILDCARD. */
	public static final boolean DEFAULT_ALLOW_LEADING_WILDCARD = true;

	/** The Constant DEFAULT_ANALYZE_WILDCARD. */
	public static final boolean DEFAULT_ANALYZE_WILDCARD = false;

	/** The query string. */
	private String queryString;

	/** The default field. */
	private String defaultField;

	/** The boost. */
	private float boost = 1.0f;

	/** The default operator. */
	private MapperQueryParser.Operator defaultOperator = QueryParser.Operator.OR;

	/** The auto generate phrase queries. */
	private boolean autoGeneratePhraseQueries = false;

	/** The allow leading wildcard. */
	private boolean allowLeadingWildcard = DEFAULT_ALLOW_LEADING_WILDCARD;

	/** The lowercase expanded terms. */
	private boolean lowercaseExpandedTerms = true;

	/** The enable position increments. */
	private boolean enablePositionIncrements = true;

	/** The phrase slop. */
	private int phraseSlop = 0;

	/** The fuzzy min sim. */
	private float fuzzyMinSim = FuzzyQuery.defaultMinSimilarity;

	/** The fuzzy prefix length. */
	private int fuzzyPrefixLength = FuzzyQuery.defaultPrefixLength;

	/** The analyze wildcard. */
	private boolean analyzeWildcard = DEFAULT_ANALYZE_WILDCARD;

	/** The escape. */
	private boolean escape = false;

	/** The default analyzer. */
	private Analyzer defaultAnalyzer = null;

	/** The forced analyzer. */
	private Analyzer forcedAnalyzer = null;

	/** The rewrite method. */
	private MultiTermQuery.RewriteMethod rewriteMethod = MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT;

	/** The minimum should match. */
	private String minimumShouldMatch;

	/**
	 * Query string.
	 *
	 * @return the string
	 */
	public String queryString() {
		return queryString;
	}

	/**
	 * Query string.
	 *
	 * @param queryString the query string
	 */
	public void queryString(String queryString) {
		this.queryString = queryString;
	}

	/**
	 * Default field.
	 *
	 * @return the string
	 */
	public String defaultField() {
		return defaultField;
	}

	/**
	 * Default field.
	 *
	 * @param defaultField the default field
	 */
	public void defaultField(String defaultField) {
		this.defaultField = defaultField;
	}

	/**
	 * Boost.
	 *
	 * @return the float
	 */
	public float boost() {
		return boost;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 */
	public void boost(float boost) {
		this.boost = boost;
	}

	/**
	 * Default operator.
	 *
	 * @return the query parser. operator
	 */
	public QueryParser.Operator defaultOperator() {
		return defaultOperator;
	}

	/**
	 * Default operator.
	 *
	 * @param defaultOperator the default operator
	 */
	public void defaultOperator(QueryParser.Operator defaultOperator) {
		this.defaultOperator = defaultOperator;
	}

	/**
	 * Auto generate phrase queries.
	 *
	 * @return true, if successful
	 */
	public boolean autoGeneratePhraseQueries() {
		return autoGeneratePhraseQueries;
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
	 * @return true, if successful
	 */
	public boolean allowLeadingWildcard() {
		return allowLeadingWildcard;
	}

	/**
	 * Allow leading wildcard.
	 *
	 * @param allowLeadingWildcard the allow leading wildcard
	 */
	public void allowLeadingWildcard(boolean allowLeadingWildcard) {
		this.allowLeadingWildcard = allowLeadingWildcard;
	}

	/**
	 * Lowercase expanded terms.
	 *
	 * @return true, if successful
	 */
	public boolean lowercaseExpandedTerms() {
		return lowercaseExpandedTerms;
	}

	/**
	 * Lowercase expanded terms.
	 *
	 * @param lowercaseExpandedTerms the lowercase expanded terms
	 */
	public void lowercaseExpandedTerms(boolean lowercaseExpandedTerms) {
		this.lowercaseExpandedTerms = lowercaseExpandedTerms;
	}

	/**
	 * Enable position increments.
	 *
	 * @return true, if successful
	 */
	public boolean enablePositionIncrements() {
		return enablePositionIncrements;
	}

	/**
	 * Enable position increments.
	 *
	 * @param enablePositionIncrements the enable position increments
	 */
	public void enablePositionIncrements(boolean enablePositionIncrements) {
		this.enablePositionIncrements = enablePositionIncrements;
	}

	/**
	 * Phrase slop.
	 *
	 * @return the int
	 */
	public int phraseSlop() {
		return phraseSlop;
	}

	/**
	 * Phrase slop.
	 *
	 * @param phraseSlop the phrase slop
	 */
	public void phraseSlop(int phraseSlop) {
		this.phraseSlop = phraseSlop;
	}

	/**
	 * Fuzzy min sim.
	 *
	 * @return the float
	 */
	public float fuzzyMinSim() {
		return fuzzyMinSim;
	}

	/**
	 * Fuzzy min sim.
	 *
	 * @param fuzzyMinSim the fuzzy min sim
	 */
	public void fuzzyMinSim(float fuzzyMinSim) {
		this.fuzzyMinSim = fuzzyMinSim;
	}

	/**
	 * Fuzzy prefix length.
	 *
	 * @return the int
	 */
	public int fuzzyPrefixLength() {
		return fuzzyPrefixLength;
	}

	/**
	 * Fuzzy prefix length.
	 *
	 * @param fuzzyPrefixLength the fuzzy prefix length
	 */
	public void fuzzyPrefixLength(int fuzzyPrefixLength) {
		this.fuzzyPrefixLength = fuzzyPrefixLength;
	}

	/**
	 * Escape.
	 *
	 * @return true, if successful
	 */
	public boolean escape() {
		return escape;
	}

	/**
	 * Escape.
	 *
	 * @param escape the escape
	 */
	public void escape(boolean escape) {
		this.escape = escape;
	}

	/**
	 * Default analyzer.
	 *
	 * @return the analyzer
	 */
	public Analyzer defaultAnalyzer() {
		return defaultAnalyzer;
	}

	/**
	 * Default analyzer.
	 *
	 * @param defaultAnalyzer the default analyzer
	 */
	public void defaultAnalyzer(Analyzer defaultAnalyzer) {
		this.defaultAnalyzer = defaultAnalyzer;
	}

	/**
	 * Forced analyzer.
	 *
	 * @return the analyzer
	 */
	public Analyzer forcedAnalyzer() {
		return forcedAnalyzer;
	}

	/**
	 * Forced analyzer.
	 *
	 * @param forcedAnalyzer the forced analyzer
	 */
	public void forcedAnalyzer(Analyzer forcedAnalyzer) {
		this.forcedAnalyzer = forcedAnalyzer;
	}

	/**
	 * Analyze wildcard.
	 *
	 * @return true, if successful
	 */
	public boolean analyzeWildcard() {
		return this.analyzeWildcard;
	}

	/**
	 * Analyze wildcard.
	 *
	 * @param analyzeWildcard the analyze wildcard
	 */
	public void analyzeWildcard(boolean analyzeWildcard) {
		this.analyzeWildcard = analyzeWildcard;
	}

	/**
	 * Rewrite method.
	 *
	 * @return the multi term query. rewrite method
	 */
	public MultiTermQuery.RewriteMethod rewriteMethod() {
		return this.rewriteMethod;
	}

	/**
	 * Rewrite method.
	 *
	 * @param rewriteMethod the rewrite method
	 */
	public void rewriteMethod(MultiTermQuery.RewriteMethod rewriteMethod) {
		this.rewriteMethod = rewriteMethod;
	}

	/**
	 * Minimum should match.
	 *
	 * @return the string
	 */
	public String minimumShouldMatch() {
		return this.minimumShouldMatch;
	}

	/**
	 * Minimum should match.
	 *
	 * @param minimumShouldMatch the minimum should match
	 */
	public void minimumShouldMatch(String minimumShouldMatch) {
		this.minimumShouldMatch = minimumShouldMatch;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		QueryParserSettings that = (QueryParserSettings) o;

		if (autoGeneratePhraseQueries != that.autoGeneratePhraseQueries())
			return false;
		if (allowLeadingWildcard != that.allowLeadingWildcard)
			return false;
		if (Float.compare(that.boost, boost) != 0)
			return false;
		if (enablePositionIncrements != that.enablePositionIncrements)
			return false;
		if (escape != that.escape)
			return false;
		if (analyzeWildcard != that.analyzeWildcard)
			return false;
		if (Float.compare(that.fuzzyMinSim, fuzzyMinSim) != 0)
			return false;
		if (fuzzyPrefixLength != that.fuzzyPrefixLength)
			return false;
		if (lowercaseExpandedTerms != that.lowercaseExpandedTerms)
			return false;
		if (phraseSlop != that.phraseSlop)
			return false;
		if (defaultAnalyzer != null ? !defaultAnalyzer.equals(that.defaultAnalyzer) : that.defaultAnalyzer != null)
			return false;
		if (forcedAnalyzer != null ? !forcedAnalyzer.equals(that.forcedAnalyzer) : that.forcedAnalyzer != null)
			return false;
		if (defaultField != null ? !defaultField.equals(that.defaultField) : that.defaultField != null)
			return false;
		if (defaultOperator != that.defaultOperator)
			return false;
		if (queryString != null ? !queryString.equals(that.queryString) : that.queryString != null)
			return false;
		if (rewriteMethod != null ? !rewriteMethod.equals(that.rewriteMethod) : that.rewriteMethod != null)
			return false;
		if (minimumShouldMatch != null ? !minimumShouldMatch.equals(that.minimumShouldMatch)
				: that.minimumShouldMatch != null)
			return false;

		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = queryString != null ? queryString.hashCode() : 0;
		result = 31 * result + (defaultField != null ? defaultField.hashCode() : 0);
		result = 31 * result + (boost != +0.0f ? Float.floatToIntBits(boost) : 0);
		result = 31 * result + (defaultOperator != null ? defaultOperator.hashCode() : 0);
		result = 31 * result + (autoGeneratePhraseQueries ? 1 : 0);
		result = 31 * result + (allowLeadingWildcard ? 1 : 0);
		result = 31 * result + (lowercaseExpandedTerms ? 1 : 0);
		result = 31 * result + (enablePositionIncrements ? 1 : 0);
		result = 31 * result + phraseSlop;
		result = 31 * result + (fuzzyMinSim != +0.0f ? Float.floatToIntBits(fuzzyMinSim) : 0);
		result = 31 * result + fuzzyPrefixLength;
		result = 31 * result + (escape ? 1 : 0);
		result = 31 * result + (defaultAnalyzer != null ? defaultAnalyzer.hashCode() : 0);
		result = 31 * result + (forcedAnalyzer != null ? forcedAnalyzer.hashCode() : 0);
		result = 31 * result + (analyzeWildcard ? 1 : 0);
		return result;
	}
}
