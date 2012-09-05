/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FuzzyLikeThisQueryParser.java 2012-7-6 14:29:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.FuzzyLikeThisQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;

import com.google.common.collect.Lists;

/**
 * The Class FuzzyLikeThisQueryParser.
 *
 * @author l.xue.nong
 */
public class FuzzyLikeThisQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "flt";

	/**
	 * Instantiates a new fuzzy like this query parser.
	 */
	@Inject
	public FuzzyLikeThisQueryParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, "fuzzy_like_this", "fuzzyLikeThis" };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		int maxNumTerms = 25;
		float boost = 1.0f;
		List<String> fields = null;
		String likeText = null;
		float minSimilarity = 0.5f;
		int prefixLength = 0;
		boolean ignoreTF = false;
		Analyzer analyzer = null;

		XContentParser.Token token;
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
					throw new QueryParsingException(parseContext.index(), "[flt] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("fields".equals(currentFieldName)) {
					fields = Lists.newArrayList();
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						fields.add(parseContext.indexName(parser.text()));
					}
				} else {
					throw new QueryParsingException(parseContext.index(), "[flt] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (likeText == null) {
			throw new QueryParsingException(parseContext.index(),
					"fuzzy_like_this requires 'like_text' to be specified");
		}

		if (analyzer == null) {
			analyzer = parseContext.mapperService().searchAnalyzer();
		}

		FuzzyLikeThisQuery query = new FuzzyLikeThisQuery(maxNumTerms, analyzer);
		if (fields == null) {

			query.addTerms(likeText, parseContext.defaultField(), minSimilarity, prefixLength);
		} else {
			for (String field : fields) {
				query.addTerms(likeText, field, minSimilarity, prefixLength);
			}
		}
		query.setBoost(boost);
		query.setIgnoreTF(ignoreTF);

		return query;
	}
}