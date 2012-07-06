/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryStringQueryParser.java 2012-7-6 14:30:42 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectFloatHashMap;

import java.io.IOException;

import org.apache.lucene.queryParser.MapperQueryParser;
import org.apache.lucene.queryParser.MultiFieldQueryParserSettings;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.queryParser.QueryParserSettings;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.Queries;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;

import com.google.common.collect.Lists;

/**
 * The Class QueryStringQueryParser.
 *
 * @author l.xue.nong
 */
public class QueryStringQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "query_string";

	/** The default analyze wildcard. */
	private final boolean defaultAnalyzeWildcard;

	/** The default allow leading wildcard. */
	private final boolean defaultAllowLeadingWildcard;

	/**
	 * Instantiates a new query string query parser.
	 *
	 * @param settings the settings
	 */
	@Inject
	public QueryStringQueryParser(Settings settings) {
		this.defaultAnalyzeWildcard = settings.getAsBoolean("indices.query.query_string.analyze_wildcard",
				QueryParserSettings.DEFAULT_ANALYZE_WILDCARD);
		this.defaultAllowLeadingWildcard = settings.getAsBoolean("indices.query.query_string.allowLeadingWildcard",
				QueryParserSettings.DEFAULT_ALLOW_LEADING_WILDCARD);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, Strings.toCamelCase(NAME) };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		MultiFieldQueryParserSettings qpSettings = new MultiFieldQueryParserSettings();
		qpSettings.defaultField(parseContext.defaultField());
		qpSettings.analyzeWildcard(defaultAnalyzeWildcard);
		qpSettings.allowLeadingWildcard(defaultAllowLeadingWildcard);

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("fields".equals(currentFieldName)) {
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						String fField = null;
						float fBoost = -1;
						char[] text = parser.textCharacters();
						int end = parser.textOffset() + parser.textLength();
						for (int i = parser.textOffset(); i < end; i++) {
							if (text[i] == '^') {
								int relativeLocation = i - parser.textOffset();
								fField = new String(text, parser.textOffset(), relativeLocation);
								fBoost = Float.parseFloat(new String(text, i + 1, parser.textLength()
										- relativeLocation - 1));
								break;
							}
						}
						if (fField == null) {
							fField = parser.text();
						}
						if (qpSettings.fields() == null) {
							qpSettings.fields(Lists.<String> newArrayList());
						}

						if (Regex.isSimpleMatchPattern(fField)) {
							for (String field : parseContext.mapperService().simpleMatchToIndexNames(fField)) {
								qpSettings.fields().add(field);
								if (fBoost != -1) {
									if (qpSettings.boosts() == null) {
										qpSettings.boosts(new TObjectFloatHashMap<String>(Constants.DEFAULT_CAPACITY,
												Constants.DEFAULT_LOAD_FACTOR, 1.0f));
									}
									qpSettings.boosts().put(field, fBoost);
								}
							}
						} else {
							qpSettings.fields().add(fField);
							if (fBoost != -1) {
								if (qpSettings.boosts() == null) {
									qpSettings.boosts(new TObjectFloatHashMap<String>(Constants.DEFAULT_CAPACITY,
											Constants.DEFAULT_LOAD_FACTOR, 1.0f));
								}
								qpSettings.boosts().put(fField, fBoost);
							}
						}
					}
				} else {
					throw new QueryParsingException(parseContext.index(), "[query_string] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("query".equals(currentFieldName)) {
					qpSettings.queryString(parser.text());
				} else if ("default_field".equals(currentFieldName) || "defaultField".equals(currentFieldName)) {
					qpSettings.defaultField(parseContext.indexName(parser.text()));
				} else if ("default_operator".equals(currentFieldName) || "defaultOperator".equals(currentFieldName)) {
					String op = parser.text();
					if ("or".equalsIgnoreCase(op)) {
						qpSettings.defaultOperator(org.apache.lucene.queryParser.QueryParser.Operator.OR);
					} else if ("and".equalsIgnoreCase(op)) {
						qpSettings.defaultOperator(org.apache.lucene.queryParser.QueryParser.Operator.AND);
					} else {
						throw new QueryParsingException(parseContext.index(), "Query default operator [" + op
								+ "] is not allowed");
					}
				} else if ("analyzer".equals(currentFieldName)) {
					NamedAnalyzer analyzer = parseContext.analysisService().analyzer(parser.text());
					if (analyzer == null) {
						throw new QueryParsingException(parseContext.index(), "[query_string] analyzer ["
								+ parser.text() + "] not found");
					}
					qpSettings.forcedAnalyzer(analyzer);
				} else if ("allow_leading_wildcard".equals(currentFieldName)
						|| "allowLeadingWildcard".equals(currentFieldName)) {
					qpSettings.allowLeadingWildcard(parser.booleanValue());
				} else if ("auto_generate_phrase_queries".equals(currentFieldName)
						|| "autoGeneratePhraseQueries".equals(currentFieldName)) {
					qpSettings.autoGeneratePhraseQueries(parser.booleanValue());
				} else if ("lowercase_expanded_terms".equals(currentFieldName)
						|| "lowercaseExpandedTerms".equals(currentFieldName)) {
					qpSettings.lowercaseExpandedTerms(parser.booleanValue());
				} else if ("enable_position_increments".equals(currentFieldName)
						|| "enablePositionIncrements".equals(currentFieldName)) {
					qpSettings.enablePositionIncrements(parser.booleanValue());
				} else if ("escape".equals(currentFieldName)) {
					qpSettings.escape(parser.booleanValue());
				} else if ("use_dis_max".equals(currentFieldName) || "useDisMax".equals(currentFieldName)) {
					qpSettings.useDisMax(parser.booleanValue());
				} else if ("fuzzy_prefix_length".equals(currentFieldName)
						|| "fuzzyPrefixLength".equals(currentFieldName)) {
					qpSettings.fuzzyPrefixLength(parser.intValue());
				} else if ("phrase_slop".equals(currentFieldName) || "phraseSlop".equals(currentFieldName)) {
					qpSettings.phraseSlop(parser.intValue());
				} else if ("fuzzy_min_sim".equals(currentFieldName) || "fuzzyMinSim".equals(currentFieldName)) {
					qpSettings.fuzzyMinSim(parser.floatValue());
				} else if ("boost".equals(currentFieldName)) {
					qpSettings.boost(parser.floatValue());
				} else if ("tie_breaker".equals(currentFieldName) || "tieBreaker".equals(currentFieldName)) {
					qpSettings.tieBreaker(parser.floatValue());
				} else if ("analyze_wildcard".equals(currentFieldName) || "analyzeWildcard".equals(currentFieldName)) {
					qpSettings.analyzeWildcard(parser.booleanValue());
				} else if ("rewrite".equals(currentFieldName)) {
					qpSettings.rewriteMethod(QueryParsers.parseRewriteMethod(parser.textOrNull()));
				} else if ("minimum_should_match".equals(currentFieldName)
						|| "minimumShouldMatch".equals(currentFieldName)) {
					qpSettings.minimumShouldMatch(parser.textOrNull());
				} else {
					throw new QueryParsingException(parseContext.index(), "[query_string] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}
		if (qpSettings.queryString() == null) {
			throw new QueryParsingException(parseContext.index(), "query_string must be provided with a [query]");
		}
		qpSettings.defaultAnalyzer(parseContext.mapperService().searchAnalyzer());

		if (qpSettings.escape()) {
			qpSettings.queryString(org.apache.lucene.queryParser.QueryParser.escape(qpSettings.queryString()));
		}

		Query query = parseContext.indexCache().queryParserCache().get(qpSettings);
		if (query != null) {
			return query;
		}

		MapperQueryParser queryParser;
		if (qpSettings.fields() != null) {
			if (qpSettings.fields().size() == 1) {
				qpSettings.defaultField(qpSettings.fields().get(0));
				queryParser = parseContext.singleQueryParser(qpSettings);
			} else {
				qpSettings.defaultField(null);
				queryParser = parseContext.multiQueryParser(qpSettings);
			}
		} else {
			queryParser = parseContext.singleQueryParser(qpSettings);
		}

		try {
			queryParser.setDefaultOperator(Operator.AND);
			query = queryParser.parse(qpSettings.queryString());
			query.setBoost(qpSettings.boost());
			query = Queries.optimizeQuery(Queries.fixNegativeQueryIfNeeded(query));
			if (query instanceof BooleanQuery) {
				Queries.applyMinimumShouldMatch((BooleanQuery) query, qpSettings.minimumShouldMatch());
			}
			parseContext.indexCache().queryParserCache().put(qpSettings, query);
			return query;
		} catch (ParseException e) {
			throw new QueryParsingException(parseContext.index(), "Failed to parse query [" + qpSettings.queryString()
					+ "]", e);
		}
	}
}
