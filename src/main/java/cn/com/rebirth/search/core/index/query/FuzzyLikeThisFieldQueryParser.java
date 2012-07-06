/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FuzzyLikeThisFieldQueryParser.java 2012-3-29 15:02:15 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.FuzzyLikeThisQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;


/**
 * The Class FuzzyLikeThisFieldQueryParser.
 *
 * @author l.xue.nong
 */
public class FuzzyLikeThisFieldQueryParser implements QueryParser {

	
	/** The Constant NAME. */
	public static final String NAME = "flt_field";

	
	/**
	 * Instantiates a new fuzzy like this field query parser.
	 */
	@Inject
	public FuzzyLikeThisFieldQueryParser() {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, "fuzzy_like_this_field", Strings.toCamelCase(NAME), "fuzzyLikeThisField" };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryParser#parse(cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		int maxNumTerms = 25;
		float boost = 1.0f;
		String likeText = null;
		float minSimilarity = 0.5f;
		int prefixLength = 0;
		boolean ignoreTF = false;
		Analyzer analyzer = null;

		XContentParser.Token token = parser.nextToken();
		if (token != XContentParser.Token.FIELD_NAME) {
			throw new QueryParsingException(parseContext.index(), "[flt_field] query malformed, no field");
		}
		String fieldName = parser.currentName();

		
		token = parser.nextToken();
		if (token != XContentParser.Token.START_OBJECT) {
			throw new QueryParsingException(parseContext.index(), "[flt_field] query malformed, no start_object");
		}

		String currentFieldName = null;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token.isValue()) {
				if ("like_text".equals(currentFieldName) || "likeText".equals(currentFieldName)) {
					likeText = parser.text();
				} else if ("max_query_terms".equals(currentFieldName) || "maxQueryTerms".equals(currentFieldName)) {
					maxNumTerms = parser.intValue();
				} else if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else if ("ignore_tf".equals(currentFieldName) || "ignoreTF".equals(currentFieldName)) {
					ignoreTF = parser.booleanValue();
				} else if ("min_similarity".equals(currentFieldName) || "minSimilarity".equals(currentFieldName)) {
					minSimilarity = parser.floatValue();
				} else if ("prefix_length".equals(currentFieldName) || "prefixLength".equals(currentFieldName)) {
					prefixLength = parser.intValue();
				} else if ("analyzer".equals(currentFieldName)) {
					analyzer = parseContext.analysisService().analyzer(parser.text());
				} else {
					throw new QueryParsingException(parseContext.index(), "[flt_field] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (likeText == null) {
			throw new QueryParsingException(parseContext.index(),
					"fuzzy_like_This_field requires 'like_text' to be specified");
		}

		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		if (smartNameFieldMappers != null) {
			if (smartNameFieldMappers.hasMapper()) {
				fieldName = smartNameFieldMappers.mapper().names().indexName();
				if (analyzer == null) {
					analyzer = smartNameFieldMappers.mapper().searchAnalyzer();
				}
			}
		}
		if (analyzer == null) {
			analyzer = parseContext.mapperService().searchAnalyzer();
		}

		FuzzyLikeThisQuery query = new FuzzyLikeThisQuery(maxNumTerms, analyzer);
		query.addTerms(likeText, fieldName, minSimilarity, prefixLength);
		query.setBoost(boost);
		query.setIgnoreTF(ignoreTF);

		
		token = parser.nextToken();
		if (token != XContentParser.Token.END_OBJECT) {
			throw new QueryParsingException(parseContext.index(), "[flt_field] query malformed, no end_object");
		}
		assert token == XContentParser.Token.END_OBJECT;

		return QueryParsers.wrapSmartNameQuery(query, smartNameFieldMappers, parseContext);
	}
}