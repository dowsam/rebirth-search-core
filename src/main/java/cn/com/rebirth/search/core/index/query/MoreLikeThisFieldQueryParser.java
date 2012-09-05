/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MoreLikeThisFieldQueryParser.java 2012-7-6 14:30:10 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.MoreLikeThisQuery;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;

import com.google.common.collect.Sets;

/**
 * The Class MoreLikeThisFieldQueryParser.
 *
 * @author l.xue.nong
 */
public class MoreLikeThisFieldQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "mlt_field";

	/**
	 * Instantiates a new more like this field query parser.
	 */
	@Inject
	public MoreLikeThisFieldQueryParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, "more_like_this_field", Strings.toCamelCase(NAME), "moreLikeThisField" };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		XContentParser.Token token = parser.nextToken();
		assert token == XContentParser.Token.FIELD_NAME;
		String fieldName = parser.currentName();

		token = parser.nextToken();
		assert token == XContentParser.Token.START_OBJECT;

		MoreLikeThisQuery mltQuery = new MoreLikeThisQuery();
		mltQuery.setSimilarity(parseContext.searchSimilarity());
		Analyzer analyzer = null;

		String currentFieldName = null;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token.isValue()) {
				if ("like_text".equals(currentFieldName)) {
					mltQuery.setLikeText(parser.text());
				} else if ("min_term_freq".equals(currentFieldName) || "minTermFreq".equals(currentFieldName)) {
					mltQuery.setMinTermFrequency(parser.intValue());
				} else if ("max_query_terms".equals(currentFieldName) || "maxQueryTerms".equals(currentFieldName)) {
					mltQuery.setMaxQueryTerms(parser.intValue());
				} else if ("min_doc_freq".equals(currentFieldName) || "minDocFreq".equals(currentFieldName)) {
					mltQuery.setMinDocFreq(parser.intValue());
				} else if ("max_doc_freq".equals(currentFieldName) || "maxDocFreq".equals(currentFieldName)) {
					mltQuery.setMaxDocFreq(parser.intValue());
				} else if ("min_word_len".equals(currentFieldName) || "minWordLen".equals(currentFieldName)) {
					mltQuery.setMinWordLen(parser.intValue());
				} else if ("max_word_len".equals(currentFieldName) || "maxWordLen".equals(currentFieldName)) {
					mltQuery.setMaxWordLen(parser.intValue());
				} else if ("boost_terms".equals(currentFieldName) || "boostTerms".equals(currentFieldName)) {
					mltQuery.setBoostTerms(true);
					mltQuery.setBoostTermsFactor(parser.floatValue());
				} else if ("percent_terms_to_match".equals(currentFieldName)
						|| "percentTermsToMatch".equals(currentFieldName)) {
					mltQuery.setPercentTermsToMatch(parser.floatValue());
				} else if ("analyzer".equals(currentFieldName)) {
					analyzer = parseContext.analysisService().analyzer(parser.text());
				} else if ("boost".equals(currentFieldName)) {
					mltQuery.setBoost(parser.floatValue());
				} else {
					throw new QueryParsingException(parseContext.index(), "[mlt_field] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("stop_words".equals(currentFieldName) || "stopWords".equals(currentFieldName)) {
					Set<String> stopWords = Sets.newHashSet();
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						stopWords.add(parser.text());
					}
					mltQuery.setStopWords(stopWords);
				} else {
					throw new QueryParsingException(parseContext.index(), "[mlt_field] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (mltQuery.getLikeText() == null) {
			throw new QueryParsingException(parseContext.index(),
					"more_like_this_field requires 'like_text' to be specified");
		}

		token = parser.nextToken();
		assert token == XContentParser.Token.END_OBJECT;

		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		if (smartNameFieldMappers != null) {
			if (smartNameFieldMappers.hasMapper()) {
				fieldName = smartNameFieldMappers.mapper().names().indexName();
			}
			if (analyzer == null) {
				analyzer = smartNameFieldMappers.searchAnalyzer();
			}
		}
		if (analyzer == null) {
			analyzer = parseContext.mapperService().searchAnalyzer();
		}
		mltQuery.setAnalyzer(analyzer);
		mltQuery.setMoreLikeFields(new String[] { fieldName });
		return QueryParsers.wrapSmartNameQuery(mltQuery, smartNameFieldMappers, parseContext);
	}
}
