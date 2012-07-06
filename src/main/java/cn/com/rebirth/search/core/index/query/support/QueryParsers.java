/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryParsers.java 2012-7-6 14:29:49 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query.support;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.search.commons.lucene.search.AndFilter;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.QueryParseContext;

import com.google.common.collect.ImmutableList;

/**
 * The Class QueryParsers.
 *
 * @author l.xue.nong
 */
public final class QueryParsers {

	/**
	 * Instantiates a new query parsers.
	 */
	private QueryParsers() {

	}

	/**
	 * Parses the rewrite method.
	 *
	 * @param rewriteMethod the rewrite method
	 * @return the multi term query. rewrite method
	 */
	public static MultiTermQuery.RewriteMethod parseRewriteMethod(@Nullable String rewriteMethod) {
		if (rewriteMethod == null) {
			return MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT;
		}
		if ("constant_score_auto".equals(rewriteMethod) || "constant_score_auto".equals(rewriteMethod)) {
			return MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT;
		}
		if ("scoring_boolean".equals(rewriteMethod) || "scoringBoolean".equals(rewriteMethod)) {
			return MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE;
		}
		if ("constant_score_boolean".equals(rewriteMethod) || "constantScoreBoolean".equals(rewriteMethod)) {
			return MultiTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE;
		}
		if ("constant_score_filter".equals(rewriteMethod) || "constantScoreFilter".equals(rewriteMethod)) {
			return MultiTermQuery.CONSTANT_SCORE_FILTER_REWRITE;
		}
		if (rewriteMethod.startsWith("top_terms_boost_")) {
			int size = Integer.parseInt(rewriteMethod.substring("top_terms_boost_".length()));
			return new MultiTermQuery.TopTermsBoostOnlyBooleanQueryRewrite(size);
		}
		if (rewriteMethod.startsWith("topTermsBoost")) {
			int size = Integer.parseInt(rewriteMethod.substring("topTermsBoost".length()));
			return new MultiTermQuery.TopTermsBoostOnlyBooleanQueryRewrite(size);
		}
		if (rewriteMethod.startsWith("top_terms_")) {
			int size = Integer.parseInt(rewriteMethod.substring("top_terms_".length()));
			return new MultiTermQuery.TopTermsScoringBooleanQueryRewrite(size);
		}
		if (rewriteMethod.startsWith("topTerms")) {
			int size = Integer.parseInt(rewriteMethod.substring("topTerms".length()));
			return new MultiTermQuery.TopTermsScoringBooleanQueryRewrite(size);
		}
		throw new RebirthIllegalArgumentException("Failed to parse rewrite_method [" + rewriteMethod + "]");
	}

	/**
	 * Wrap smart name query.
	 *
	 * @param query the query
	 * @param smartFieldMappers the smart field mappers
	 * @param parseContext the parse context
	 * @return the query
	 */
	public static Query wrapSmartNameQuery(Query query,
			@Nullable MapperService.SmartNameFieldMappers smartFieldMappers, QueryParseContext parseContext) {
		if (query == null) {
			return null;
		}
		if (smartFieldMappers == null) {
			return query;
		}
		if (!smartFieldMappers.explicitTypeInNameWithDocMapper()) {
			return query;
		}
		DocumentMapper docMapper = smartFieldMappers.docMapper();
		return new FilteredQuery(query, parseContext.cacheFilter(docMapper.typeFilter(), null));
	}

	/**
	 * Wrap smart name filter.
	 *
	 * @param filter the filter
	 * @param smartFieldMappers the smart field mappers
	 * @param parseContext the parse context
	 * @return the filter
	 */
	public static Filter wrapSmartNameFilter(Filter filter,
			@Nullable MapperService.SmartNameFieldMappers smartFieldMappers, QueryParseContext parseContext) {
		if (smartFieldMappers == null) {
			return filter;
		}
		if (!smartFieldMappers.explicitTypeInNameWithDocMapper()) {
			return filter;
		}
		DocumentMapper docMapper = smartFieldMappers.docMapper();
		return new AndFilter(ImmutableList.of(parseContext.cacheFilter(docMapper.typeFilter(), null), filter));
	}
}
