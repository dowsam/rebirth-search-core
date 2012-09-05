/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FuzzyQueryParser.java 2012-7-6 14:30:23 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;

/**
 * The Class FuzzyQueryParser.
 *
 * @author l.xue.nong
 */
public class FuzzyQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "fuzzy";

	/**
	 * Instantiates a new fuzzy query parser.
	 */
	@Inject
	public FuzzyQueryParser() {
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
			throw new QueryParsingException(parseContext.index(), "[fuzzy] query malformed, no field");
		}
		String fieldName = parser.currentName();

		String value = null;
		float boost = 1.0f;
		String minSimilarity = "0.5";
		int prefixLength = FuzzyQuery.defaultPrefixLength;
		int maxExpansions = FuzzyQuery.defaultMaxExpansions;
		token = parser.nextToken();
		if (token == XContentParser.Token.START_OBJECT) {
			String currentFieldName = null;
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else {
					if ("term".equals(currentFieldName)) {
						value = parser.text();
					} else if ("value".equals(currentFieldName)) {
						value = parser.text();
					} else if ("boost".equals(currentFieldName)) {
						boost = parser.floatValue();
					} else if ("min_similarity".equals(currentFieldName) || "minSimilarity".equals(currentFieldName)) {
						minSimilarity = parser.text();
					} else if ("prefix_length".equals(currentFieldName) || "prefixLength".equals(currentFieldName)) {
						prefixLength = parser.intValue();
					} else if ("max_expansions".equals(currentFieldName) || "maxExpansions".equals(currentFieldName)) {
						maxExpansions = parser.intValue();
					} else {
						throw new QueryParsingException(parseContext.index(), "[fuzzy] query does not support ["
								+ currentFieldName + "]");
					}
				}
			}
			parser.nextToken();
		} else {
			value = parser.text();

			parser.nextToken();
		}

		if (value == null) {
			throw new QueryParsingException(parseContext.index(), "No value specified for fuzzy query");
		}

		Query query = null;
		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		if (smartNameFieldMappers != null) {
			if (smartNameFieldMappers.hasMapper()) {
				query = smartNameFieldMappers.mapper().fuzzyQuery(value, minSimilarity, prefixLength, maxExpansions);
			}
		}
		if (query == null) {
			query = new FuzzyQuery(new Term(fieldName, value), Float.parseFloat(minSimilarity), prefixLength,
					maxExpansions);
		}
		query.setBoost(boost);

		return QueryParsers.wrapSmartNameQuery(query, smartNameFieldMappers, parseContext);
	}
}
