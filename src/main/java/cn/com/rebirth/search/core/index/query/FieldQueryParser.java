/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FieldQueryParser.java 2012-7-6 14:28:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.queryParser.MapperQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParserSettings;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.Queries;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;

/**
 * The Class FieldQueryParser.
 *
 * @author l.xue.nong
 */
public class FieldQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "field";

	/** The default analyze wildcard. */
	private final boolean defaultAnalyzeWildcard;

	/** The default allow leading wildcard. */
	private final boolean defaultAllowLeadingWildcard;

	/**
	 * Instantiates a new field query parser.
	 *
	 * @param settings the settings
	 */
	@Inject
	public FieldQueryParser(Settings settings) {
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
		return new String[] { NAME };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		XContentParser.Token token = parser.nextToken();
		if (token != XContentParser.Token.FIELD_NAME) {
			throw new QueryParsingException(parseContext.index(), "[field] query malformed, no field");
		}
		String fieldName = parser.currentName();

		QueryParserSettings qpSettings = new QueryParserSettings();
		qpSettings.defaultField(fieldName);
		qpSettings.analyzeWildcard(defaultAnalyzeWildcard);
		qpSettings.allowLeadingWildcard(defaultAllowLeadingWildcard);

		token = parser.nextToken();
		if (token == XContentParser.Token.START_OBJECT) {
			String currentFieldName = null;
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else if (token.isValue()) {
					if ("query".equals(currentFieldName)) {
						qpSettings.queryString(parser.text());
					} else if ("boost".equals(currentFieldName)) {
						qpSettings.boost(parser.floatValue());
					} else if ("enable_position_increments".equals(currentFieldName)
							|| "enablePositionIncrements".equals(currentFieldName)) {
						qpSettings.enablePositionIncrements(parser.booleanValue());
					} else if ("allow_leading_wildcard".equals(currentFieldName)
							|| "allowLeadingWildcard".equals(currentFieldName)) {
						qpSettings.allowLeadingWildcard(parser.booleanValue());
					} else if ("auto_generate_phrase_queries".equals(currentFieldName)
							|| "autoGeneratePhraseQueries".equals(currentFieldName)) {
						qpSettings.autoGeneratePhraseQueries(parser.booleanValue());
					} else if ("lowercase_expanded_terms".equals(currentFieldName)
							|| "lowercaseExpandedTerms".equals(currentFieldName)) {
						qpSettings.lowercaseExpandedTerms(parser.booleanValue());
					} else if ("phrase_slop".equals(currentFieldName) || "phraseSlop".equals(currentFieldName)) {
						qpSettings.phraseSlop(parser.intValue());
					} else if ("analyzer".equals(currentFieldName)) {
						NamedAnalyzer analyzer = parseContext.analysisService().analyzer(parser.text());
						if (analyzer == null) {
							throw new QueryParsingException(parseContext.index(), "[query_string] analyzer ["
									+ parser.text() + "] not found");
						}
						qpSettings.forcedAnalyzer(analyzer);
					} else if ("default_operator".equals(currentFieldName)
							|| "defaultOperator".equals(currentFieldName)) {
						String op = parser.text();
						if ("or".equalsIgnoreCase(op)) {
							qpSettings.defaultOperator(org.apache.lucene.queryParser.QueryParser.Operator.OR);
						} else if ("and".equalsIgnoreCase(op)) {
							qpSettings.defaultOperator(org.apache.lucene.queryParser.QueryParser.Operator.AND);
						} else {
							throw new QueryParsingException(parseContext.index(), "Query default operator [" + op
									+ "] is not allowed");
						}
					} else if ("fuzzy_min_sim".equals(currentFieldName) || "fuzzyMinSim".equals(currentFieldName)) {
						qpSettings.fuzzyMinSim(parser.floatValue());
					} else if ("fuzzy_prefix_length".equals(currentFieldName)
							|| "fuzzyPrefixLength".equals(currentFieldName)) {
						qpSettings.fuzzyPrefixLength(parser.intValue());
					} else if ("escape".equals(currentFieldName)) {
						qpSettings.escape(parser.booleanValue());
					} else if ("analyze_wildcard".equals(currentFieldName)
							|| "analyzeWildcard".equals(currentFieldName)) {
						qpSettings.analyzeWildcard(parser.booleanValue());
					} else if ("rewrite".equals(currentFieldName)) {
						qpSettings.rewriteMethod(QueryParsers.parseRewriteMethod(parser.textOrNull()));
					} else if ("minimum_should_match".equals(currentFieldName)
							|| "minimumShouldMatch".equals(currentFieldName)) {
						qpSettings.minimumShouldMatch(parser.textOrNull());
					} else {
						throw new QueryParsingException(parseContext.index(), "[field] query does not support ["
								+ currentFieldName + "]");
					}
				}
			}
			parser.nextToken();
		} else {
			qpSettings.queryString(parser.text());

			parser.nextToken();
		}

		qpSettings.defaultAnalyzer(parseContext.mapperService().searchAnalyzer());

		if (qpSettings.queryString() == null) {
			throw new QueryParsingException(parseContext.index(), "No value specified for term query");
		}

		if (qpSettings.escape()) {
			qpSettings.queryString(org.apache.lucene.queryParser.QueryParser.escape(qpSettings.queryString()));
		}

		Query query = parseContext.indexCache().queryParserCache().get(qpSettings);
		if (query != null) {
			return query;
		}

		MapperQueryParser queryParser = parseContext.singleQueryParser(qpSettings);

		try {
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