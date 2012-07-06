/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core QueryBuilders.java 2012-3-29 15:01:01 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import cn.com.rebirth.commons.Nullable;


/**
 * The Class QueryBuilders.
 *
 * @author l.xue.nong
 */
public abstract class QueryBuilders {

	
	/**
	 * Match all query.
	 *
	 * @return the match all query builder
	 */
	public static MatchAllQueryBuilder matchAllQuery() {
		return new MatchAllQueryBuilder();
	}

	
	/**
	 * Text.
	 *
	 * @param name the name
	 * @param text the text
	 * @return the text query builder
	 */
	public static TextQueryBuilder text(String name, Object text) {
		return textQuery(name, text);
	}

	
	/**
	 * Text query.
	 *
	 * @param name the name
	 * @param text the text
	 * @return the text query builder
	 */
	public static TextQueryBuilder textQuery(String name, Object text) {
		return new TextQueryBuilder(name, text).type(TextQueryBuilder.Type.BOOLEAN);
	}

	
	/**
	 * Text phrase.
	 *
	 * @param name the name
	 * @param text the text
	 * @return the text query builder
	 */
	public static TextQueryBuilder textPhrase(String name, Object text) {
		return textPhraseQuery(name, text);
	}

	
	/**
	 * Text phrase query.
	 *
	 * @param name the name
	 * @param text the text
	 * @return the text query builder
	 */
	public static TextQueryBuilder textPhraseQuery(String name, Object text) {
		return new TextQueryBuilder(name, text).type(TextQueryBuilder.Type.PHRASE);
	}

	
	/**
	 * Text phrase prefix.
	 *
	 * @param name the name
	 * @param text the text
	 * @return the text query builder
	 */
	public static TextQueryBuilder textPhrasePrefix(String name, Object text) {
		return textPhrasePrefixQuery(name, text);
	}

	
	/**
	 * Text phrase prefix query.
	 *
	 * @param name the name
	 * @param text the text
	 * @return the text query builder
	 */
	public static TextQueryBuilder textPhrasePrefixQuery(String name, Object text) {
		return new TextQueryBuilder(name, text).type(TextQueryBuilder.Type.PHRASE_PREFIX);
	}

	
	/**
	 * Dis max query.
	 *
	 * @return the dis max query builder
	 */
	public static DisMaxQueryBuilder disMaxQuery() {
		return new DisMaxQueryBuilder();
	}

	
	/**
	 * Ids query.
	 *
	 * @param types the types
	 * @return the ids query builder
	 */
	public static IdsQueryBuilder idsQuery(@Nullable String... types) {
		return new IdsQueryBuilder(types);
	}

	
	/**
	 * Term query.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the term query builder
	 */
	public static TermQueryBuilder termQuery(String name, String value) {
		return new TermQueryBuilder(name, value);
	}

	
	/**
	 * Term query.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the term query builder
	 */
	public static TermQueryBuilder termQuery(String name, int value) {
		return new TermQueryBuilder(name, value);
	}

	
	/**
	 * Term query.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the term query builder
	 */
	public static TermQueryBuilder termQuery(String name, long value) {
		return new TermQueryBuilder(name, value);
	}

	
	/**
	 * Term query.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the term query builder
	 */
	public static TermQueryBuilder termQuery(String name, float value) {
		return new TermQueryBuilder(name, value);
	}

	
	/**
	 * Term query.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the term query builder
	 */
	public static TermQueryBuilder termQuery(String name, double value) {
		return new TermQueryBuilder(name, value);
	}

	
	/**
	 * Term query.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the term query builder
	 */
	public static TermQueryBuilder termQuery(String name, boolean value) {
		return new TermQueryBuilder(name, value);
	}

	
	/**
	 * Term query.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the term query builder
	 */
	public static TermQueryBuilder termQuery(String name, Object value) {
		return new TermQueryBuilder(name, value);
	}

	
	/**
	 * Fuzzy query.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the fuzzy query builder
	 */
	public static FuzzyQueryBuilder fuzzyQuery(String name, String value) {
		return new FuzzyQueryBuilder(name, value);
	}

	
	/**
	 * Field query.
	 *
	 * @param name the name
	 * @param query the query
	 * @return the field query builder
	 */
	public static FieldQueryBuilder fieldQuery(String name, String query) {
		return new FieldQueryBuilder(name, query);
	}

	
	/**
	 * Field query.
	 *
	 * @param name the name
	 * @param query the query
	 * @return the field query builder
	 */
	public static FieldQueryBuilder fieldQuery(String name, int query) {
		return new FieldQueryBuilder(name, query);
	}

	
	/**
	 * Field query.
	 *
	 * @param name the name
	 * @param query the query
	 * @return the field query builder
	 */
	public static FieldQueryBuilder fieldQuery(String name, long query) {
		return new FieldQueryBuilder(name, query);
	}

	
	/**
	 * Field query.
	 *
	 * @param name the name
	 * @param query the query
	 * @return the field query builder
	 */
	public static FieldQueryBuilder fieldQuery(String name, float query) {
		return new FieldQueryBuilder(name, query);
	}

	
	/**
	 * Field query.
	 *
	 * @param name the name
	 * @param query the query
	 * @return the field query builder
	 */
	public static FieldQueryBuilder fieldQuery(String name, double query) {
		return new FieldQueryBuilder(name, query);
	}

	
	/**
	 * Field query.
	 *
	 * @param name the name
	 * @param query the query
	 * @return the field query builder
	 */
	public static FieldQueryBuilder fieldQuery(String name, boolean query) {
		return new FieldQueryBuilder(name, query);
	}

	
	/**
	 * Field query.
	 *
	 * @param name the name
	 * @param query the query
	 * @return the field query builder
	 */
	public static FieldQueryBuilder fieldQuery(String name, Object query) {
		return new FieldQueryBuilder(name, query);
	}

	
	/**
	 * Prefix query.
	 *
	 * @param name the name
	 * @param prefix the prefix
	 * @return the prefix query builder
	 */
	public static PrefixQueryBuilder prefixQuery(String name, String prefix) {
		return new PrefixQueryBuilder(name, prefix);
	}

	
	/**
	 * Range query.
	 *
	 * @param name the name
	 * @return the range query builder
	 */
	public static RangeQueryBuilder rangeQuery(String name) {
		return new RangeQueryBuilder(name);
	}

	
	/**
	 * Wildcard query.
	 *
	 * @param name the name
	 * @param query the query
	 * @return the wildcard query builder
	 */
	public static WildcardQueryBuilder wildcardQuery(String name, String query) {
		return new WildcardQueryBuilder(name, query);
	}

	
	/**
	 * Query string.
	 *
	 * @param queryString the query string
	 * @return the query string query builder
	 */
	public static QueryStringQueryBuilder queryString(String queryString) {
		return new QueryStringQueryBuilder(queryString);
	}

	
	/**
	 * Boosting query.
	 *
	 * @return the boosting query builder
	 */
	public static BoostingQueryBuilder boostingQuery() {
		return new BoostingQueryBuilder();
	}

	
	/**
	 * Bool query.
	 *
	 * @return the bool query builder
	 */
	public static BoolQueryBuilder boolQuery() {
		return new BoolQueryBuilder();
	}

	
	/**
	 * Span term query.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the span term query builder
	 */
	public static SpanTermQueryBuilder spanTermQuery(String name, String value) {
		return new SpanTermQueryBuilder(name, value);
	}

	
	/**
	 * Span term query.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the span term query builder
	 */
	public static SpanTermQueryBuilder spanTermQuery(String name, int value) {
		return new SpanTermQueryBuilder(name, value);
	}

	
	/**
	 * Span term query.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the span term query builder
	 */
	public static SpanTermQueryBuilder spanTermQuery(String name, long value) {
		return new SpanTermQueryBuilder(name, value);
	}

	
	/**
	 * Span term query.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the span term query builder
	 */
	public static SpanTermQueryBuilder spanTermQuery(String name, float value) {
		return new SpanTermQueryBuilder(name, value);
	}

	
	/**
	 * Span term query.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the span term query builder
	 */
	public static SpanTermQueryBuilder spanTermQuery(String name, double value) {
		return new SpanTermQueryBuilder(name, value);
	}

	
	/**
	 * Span first query.
	 *
	 * @param match the match
	 * @param end the end
	 * @return the span first query builder
	 */
	public static SpanFirstQueryBuilder spanFirstQuery(SpanQueryBuilder match, int end) {
		return new SpanFirstQueryBuilder(match, end);
	}

	
	/**
	 * Span near query.
	 *
	 * @return the span near query builder
	 */
	public static SpanNearQueryBuilder spanNearQuery() {
		return new SpanNearQueryBuilder();
	}

	
	/**
	 * Span not query.
	 *
	 * @return the span not query builder
	 */
	public static SpanNotQueryBuilder spanNotQuery() {
		return new SpanNotQueryBuilder();
	}

	
	/**
	 * Span or query.
	 *
	 * @return the span or query builder
	 */
	public static SpanOrQueryBuilder spanOrQuery() {
		return new SpanOrQueryBuilder();
	}

	
	/**
	 * Field masking span query.
	 *
	 * @param query the query
	 * @param field the field
	 * @return the field masking span query builder
	 */
	public static FieldMaskingSpanQueryBuilder fieldMaskingSpanQuery(SpanQueryBuilder query, String field) {
		return new FieldMaskingSpanQueryBuilder(query, field);
	}

	
	/**
	 * Filtered.
	 *
	 * @param queryBuilder the query builder
	 * @param filterBuilder the filter builder
	 * @return the filtered query builder
	 */
	public static FilteredQueryBuilder filtered(QueryBuilder queryBuilder, FilterBuilder filterBuilder) {
		return new FilteredQueryBuilder(queryBuilder, filterBuilder);
	}

	
	/**
	 * Filtered query.
	 *
	 * @param queryBuilder the query builder
	 * @param filterBuilder the filter builder
	 * @return the filtered query builder
	 */
	public static FilteredQueryBuilder filteredQuery(QueryBuilder queryBuilder, FilterBuilder filterBuilder) {
		return new FilteredQueryBuilder(queryBuilder, filterBuilder);
	}

	
	/**
	 * Constant score query.
	 *
	 * @param filterBuilder the filter builder
	 * @return the constant score query builder
	 */
	public static ConstantScoreQueryBuilder constantScoreQuery(FilterBuilder filterBuilder) {
		return new ConstantScoreQueryBuilder(filterBuilder);
	}

	
	/**
	 * Custom boost factor query.
	 *
	 * @param queryBuilder the query builder
	 * @return the custom boost factor query builder
	 */
	public static CustomBoostFactorQueryBuilder customBoostFactorQuery(QueryBuilder queryBuilder) {
		return new CustomBoostFactorQueryBuilder(queryBuilder);
	}

	
	/**
	 * Custom score query.
	 *
	 * @param queryBuilder the query builder
	 * @return the custom score query builder
	 */
	public static CustomScoreQueryBuilder customScoreQuery(QueryBuilder queryBuilder) {
		return new CustomScoreQueryBuilder(queryBuilder);
	}

	
	/**
	 * Custom filters score query.
	 *
	 * @param queryBuilder the query builder
	 * @return the custom filters score query builder
	 */
	public static CustomFiltersScoreQueryBuilder customFiltersScoreQuery(QueryBuilder queryBuilder) {
		return new CustomFiltersScoreQueryBuilder(queryBuilder);
	}

	
	/**
	 * More like this query.
	 *
	 * @param fields the fields
	 * @return the more like this query builder
	 */
	public static MoreLikeThisQueryBuilder moreLikeThisQuery(String... fields) {
		return new MoreLikeThisQueryBuilder(fields);
	}

	
	/**
	 * More like this query.
	 *
	 * @return the more like this query builder
	 */
	public static MoreLikeThisQueryBuilder moreLikeThisQuery() {
		return new MoreLikeThisQueryBuilder();
	}

	
	/**
	 * Fuzzy like this query.
	 *
	 * @param fields the fields
	 * @return the fuzzy like this query builder
	 */
	public static FuzzyLikeThisQueryBuilder fuzzyLikeThisQuery(String... fields) {
		return new FuzzyLikeThisQueryBuilder(fields);
	}

	
	/**
	 * Fuzzy like this query.
	 *
	 * @return the fuzzy like this query builder
	 */
	public static FuzzyLikeThisQueryBuilder fuzzyLikeThisQuery() {
		return new FuzzyLikeThisQueryBuilder();
	}

	
	/**
	 * Fuzzy like this field query.
	 *
	 * @param name the name
	 * @return the fuzzy like this field query builder
	 */
	public static FuzzyLikeThisFieldQueryBuilder fuzzyLikeThisFieldQuery(String name) {
		return new FuzzyLikeThisFieldQueryBuilder(name);
	}

	
	/**
	 * More like this field query.
	 *
	 * @param name the name
	 * @return the more like this field query builder
	 */
	public static MoreLikeThisFieldQueryBuilder moreLikeThisFieldQuery(String name) {
		return new MoreLikeThisFieldQueryBuilder(name);
	}

	
	/**
	 * Top children query.
	 *
	 * @param type the type
	 * @param query the query
	 * @return the top children query builder
	 */
	public static TopChildrenQueryBuilder topChildrenQuery(String type, QueryBuilder query) {
		return new TopChildrenQueryBuilder(type, query);
	}

	
	/**
	 * Checks for child query.
	 *
	 * @param type the type
	 * @param query the query
	 * @return the checks for child query builder
	 */
	public static HasChildQueryBuilder hasChildQuery(String type, QueryBuilder query) {
		return new HasChildQueryBuilder(type, query);
	}

	
	/**
	 * Nested query.
	 *
	 * @param path the path
	 * @param query the query
	 * @return the nested query builder
	 */
	public static NestedQueryBuilder nestedQuery(String path, QueryBuilder query) {
		return new NestedQueryBuilder(path, query);
	}

	
	/**
	 * Nested query.
	 *
	 * @param path the path
	 * @param filter the filter
	 * @return the nested query builder
	 */
	public static NestedQueryBuilder nestedQuery(String path, FilterBuilder filter) {
		return new NestedQueryBuilder(path, filter);
	}

	
	/**
	 * Terms query.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms query builder
	 */
	public static TermsQueryBuilder termsQuery(String name, String... values) {
		return new TermsQueryBuilder(name, values);
	}

	
	/**
	 * Terms query.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms query builder
	 */
	public static TermsQueryBuilder termsQuery(String name, int... values) {
		return new TermsQueryBuilder(name, values);
	}

	
	/**
	 * Terms query.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms query builder
	 */
	public static TermsQueryBuilder termsQuery(String name, long... values) {
		return new TermsQueryBuilder(name, values);
	}

	
	/**
	 * Terms query.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms query builder
	 */
	public static TermsQueryBuilder termsQuery(String name, float... values) {
		return new TermsQueryBuilder(name, values);
	}

	
	/**
	 * Terms query.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms query builder
	 */
	public static TermsQueryBuilder termsQuery(String name, double... values) {
		return new TermsQueryBuilder(name, values);
	}

	
	/**
	 * Terms query.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms query builder
	 */
	public static TermsQueryBuilder termsQuery(String name, Object... values) {
		return new TermsQueryBuilder(name, values);
	}

	
	/**
	 * In query.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms query builder
	 */
	public static TermsQueryBuilder inQuery(String name, String... values) {
		return new TermsQueryBuilder(name, values);
	}

	
	/**
	 * In query.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms query builder
	 */
	public static TermsQueryBuilder inQuery(String name, int... values) {
		return new TermsQueryBuilder(name, values);
	}

	
	/**
	 * In query.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms query builder
	 */
	public static TermsQueryBuilder inQuery(String name, long... values) {
		return new TermsQueryBuilder(name, values);
	}

	
	/**
	 * In query.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms query builder
	 */
	public static TermsQueryBuilder inQuery(String name, float... values) {
		return new TermsQueryBuilder(name, values);
	}

	
	/**
	 * In query.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms query builder
	 */
	public static TermsQueryBuilder inQuery(String name, double... values) {
		return new TermsQueryBuilder(name, values);
	}

	
	/**
	 * In query.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the terms query builder
	 */
	public static TermsQueryBuilder inQuery(String name, Object... values) {
		return new TermsQueryBuilder(name, values);
	}

	
	/**
	 * Indices query.
	 *
	 * @param queryBuilder the query builder
	 * @param indices the indices
	 * @return the indices query builder
	 */
	public static IndicesQueryBuilder indicesQuery(QueryBuilder queryBuilder, String... indices) {
		return new IndicesQueryBuilder(queryBuilder, indices);
	}

	
	/**
	 * Wrapper query.
	 *
	 * @param source the source
	 * @return the wrapper query builder
	 */
	public static WrapperQueryBuilder wrapperQuery(String source) {
		return new WrapperQueryBuilder(source);
	}

	
	/**
	 * Wrapper query.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @return the wrapper query builder
	 */
	public static WrapperQueryBuilder wrapperQuery(byte[] source, int offset, int length) {
		return new WrapperQueryBuilder(source, offset, length);
	}

	
	/**
	 * Instantiates a new query builders.
	 */
	private QueryBuilders() {

	}
}
