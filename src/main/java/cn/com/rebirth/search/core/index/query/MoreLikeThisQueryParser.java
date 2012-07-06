/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MoreLikeThisQueryParser.java 2012-3-29 15:00:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.MoreLikeThisQuery;
import cn.com.rebirth.search.commons.xcontent.XContentParser;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * The Class MoreLikeThisQueryParser.
 *
 * @author l.xue.nong
 */
public class MoreLikeThisQueryParser implements QueryParser {

	
	/** The Constant NAME. */
	public static final String NAME = "mlt";

	
	/**
	 * Instantiates a new more like this query parser.
	 */
	@Inject
	public MoreLikeThisQueryParser() {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, "more_like_this", "moreLikeThis" };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryParser#parse(cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		MoreLikeThisQuery mltQuery = new MoreLikeThisQuery();
		mltQuery.setMoreLikeFields(new String[] { parseContext.defaultField() });
		mltQuery.setSimilarity(parseContext.searchSimilarity());
		Analyzer analyzer = null;

		XContentParser.Token token;
		String currentFieldName = null;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token.isValue()) {
				if ("like_text".equals(currentFieldName) || "likeText".equals(currentFieldName)) {
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
					throw new QueryParsingException(parseContext.index(), "[mlt] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("stop_words".equals(currentFieldName) || "stopWords".equals(currentFieldName)) {
					Set<String> stopWords = Sets.newHashSet();
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						stopWords.add(parser.text());
					}
					mltQuery.setStopWords(stopWords);
				} else if ("fields".equals(currentFieldName)) {
					List<String> fields = Lists.newArrayList();
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						fields.add(parseContext.indexName(parser.text()));
					}
					mltQuery.setMoreLikeFields(fields.toArray(new String[fields.size()]));
				} else {
					throw new QueryParsingException(parseContext.index(), "[mlt] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (mltQuery.getLikeText() == null) {
			throw new QueryParsingException(parseContext.index(), "more_like_this requires 'like_text' to be specified");
		}
		if (mltQuery.getMoreLikeFields() == null || mltQuery.getMoreLikeFields().length == 0) {
			throw new QueryParsingException(parseContext.index(), "more_like_this requires 'fields' to be specified");
		}

		if (analyzer == null) {
			analyzer = parseContext.mapperService().searchAnalyzer();
		}

		mltQuery.setAnalyzer(analyzer);
		return mltQuery;
	}
}