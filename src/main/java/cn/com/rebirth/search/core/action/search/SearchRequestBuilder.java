/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchRequestBuilder.java 2012-7-6 14:29:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search;

import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.support.BaseRequestBuilder;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.index.query.QueryBuilder;
import cn.com.rebirth.search.core.search.Scroll;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilder;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;
import cn.com.rebirth.search.core.search.highlight.HighlightBuilder;
import cn.com.rebirth.search.core.search.sort.SortBuilder;
import cn.com.rebirth.search.core.search.sort.SortOrder;

/**
 * The Class SearchRequestBuilder.
 *
 * @author l.xue.nong
 */
public class SearchRequestBuilder extends BaseRequestBuilder<SearchRequest, SearchResponse> {

	/** The source builder. */
	private SearchSourceBuilder sourceBuilder;

	/**
	 * Instantiates a new search request builder.
	 *
	 * @param client the client
	 */
	public SearchRequestBuilder(Client client) {
		super(client, new SearchRequest());
	}

	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the search request builder
	 */
	public SearchRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	/**
	 * Sets the types.
	 *
	 * @param types the types
	 * @return the search request builder
	 */
	public SearchRequestBuilder setTypes(String... types) {
		request.types(types);
		return this;
	}

	/**
	 * Sets the search type.
	 *
	 * @param searchType the search type
	 * @return the search request builder
	 */
	public SearchRequestBuilder setSearchType(SearchType searchType) {
		request.searchType(searchType);
		return this;
	}

	/**
	 * Sets the search type.
	 *
	 * @param searchType the search type
	 * @return the search request builder
	 * @throws RebirthIllegalArgumentException the rebirth illegal argument exception
	 */
	public SearchRequestBuilder setSearchType(String searchType) throws RebirthIllegalArgumentException {
		request.searchType(searchType);
		return this;
	}

	/**
	 * Sets the scroll.
	 *
	 * @param scroll the scroll
	 * @return the search request builder
	 */
	public SearchRequestBuilder setScroll(Scroll scroll) {
		request.scroll(scroll);
		return this;
	}

	/**
	 * Sets the scroll.
	 *
	 * @param keepAlive the keep alive
	 * @return the search request builder
	 */
	public SearchRequestBuilder setScroll(TimeValue keepAlive) {
		request.scroll(keepAlive);
		return this;
	}

	/**
	 * Sets the scroll.
	 *
	 * @param keepAlive the keep alive
	 * @return the search request builder
	 */
	public SearchRequestBuilder setScroll(String keepAlive) {
		request.scroll(keepAlive);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the search request builder
	 */
	public SearchRequestBuilder setTimeout(TimeValue timeout) {
		sourceBuilder().timeout(timeout);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the search request builder
	 */
	public SearchRequestBuilder setTimeout(String timeout) {
		sourceBuilder().timeout(timeout);
		return this;
	}

	/**
	 * Sets the query hint.
	 *
	 * @param queryHint the query hint
	 * @return the search request builder
	 */
	public SearchRequestBuilder setQueryHint(String queryHint) {
		request.queryHint(queryHint);
		return this;
	}

	/**
	 * Sets the routing.
	 *
	 * @param routing the routing
	 * @return the search request builder
	 */
	public SearchRequestBuilder setRouting(String routing) {
		request.routing(routing);
		return this;
	}

	/**
	 * Sets the routing.
	 *
	 * @param routing the routing
	 * @return the search request builder
	 */
	public SearchRequestBuilder setRouting(String... routing) {
		request.routing(routing);
		return this;
	}

	/**
	 * Sets the preference.
	 *
	 * @param preference the preference
	 * @return the search request builder
	 */
	public SearchRequestBuilder setPreference(String preference) {
		request.preference(preference);
		return this;
	}

	/**
	 * Sets the operation threading.
	 *
	 * @param operationThreading the operation threading
	 * @return the search request builder
	 */
	public SearchRequestBuilder setOperationThreading(SearchOperationThreading operationThreading) {
		request.operationThreading(operationThreading);
		return this;
	}

	/**
	 * Sets the operation threading.
	 *
	 * @param operationThreading the operation threading
	 * @return the search request builder
	 */
	public SearchRequestBuilder setOperationThreading(String operationThreading) {
		request.operationThreading(operationThreading);
		return this;
	}

	/**
	 * Sets the listener threaded.
	 *
	 * @param listenerThreaded the listener threaded
	 * @return the search request builder
	 */
	public SearchRequestBuilder setListenerThreaded(boolean listenerThreaded) {
		request.listenerThreaded(listenerThreaded);
		return this;
	}

	/**
	 * Sets the query.
	 *
	 * @param queryBuilder the query builder
	 * @return the search request builder
	 */
	public SearchRequestBuilder setQuery(QueryBuilder queryBuilder) {
		sourceBuilder().query(queryBuilder);
		return this;
	}

	/**
	 * Sets the query.
	 *
	 * @param query the query
	 * @return the search request builder
	 */
	public SearchRequestBuilder setQuery(String query) {
		sourceBuilder().query(query);
		return this;
	}

	/**
	 * Sets the query.
	 *
	 * @param queryBinary the query binary
	 * @return the search request builder
	 */
	public SearchRequestBuilder setQuery(byte[] queryBinary) {
		sourceBuilder().query(queryBinary);
		return this;
	}

	/**
	 * Sets the query.
	 *
	 * @param queryBinary the query binary
	 * @param queryBinaryOffset the query binary offset
	 * @param queryBinaryLength the query binary length
	 * @return the search request builder
	 */
	public SearchRequestBuilder setQuery(byte[] queryBinary, int queryBinaryOffset, int queryBinaryLength) {
		sourceBuilder().query(queryBinary, queryBinaryOffset, queryBinaryLength);
		return this;
	}

	/**
	 * Sets the query.
	 *
	 * @param query the query
	 * @return the search request builder
	 */
	public SearchRequestBuilder setQuery(XContentBuilder query) {
		sourceBuilder().query(query);
		return this;
	}

	/**
	 * Sets the query.
	 *
	 * @param query the query
	 * @return the search request builder
	 */
	public SearchRequestBuilder setQuery(Map query) {
		sourceBuilder().query(query);
		return this;
	}

	/**
	 * Sets the filter.
	 *
	 * @param filter the filter
	 * @return the search request builder
	 */
	public SearchRequestBuilder setFilter(FilterBuilder filter) {
		sourceBuilder().filter(filter);
		return this;
	}

	/**
	 * Sets the filter.
	 *
	 * @param filter the filter
	 * @return the search request builder
	 */
	public SearchRequestBuilder setFilter(String filter) {
		sourceBuilder().filter(filter);
		return this;
	}

	/**
	 * Sets the filter.
	 *
	 * @param filter the filter
	 * @return the search request builder
	 */
	public SearchRequestBuilder setFilter(byte[] filter) {
		sourceBuilder().filter(filter);
		return this;
	}

	/**
	 * Sets the filter.
	 *
	 * @param filter the filter
	 * @param filterOffset the filter offset
	 * @param filterLength the filter length
	 * @return the search request builder
	 */
	public SearchRequestBuilder setFilter(byte[] filter, int filterOffset, int filterLength) {
		sourceBuilder().filter(filter, filterOffset, filterLength);
		return this;
	}

	/**
	 * Sets the filter.
	 *
	 * @param filter the filter
	 * @return the search request builder
	 */
	public SearchRequestBuilder setFilter(XContentBuilder filter) {
		sourceBuilder().filter(filter);
		return this;
	}

	/**
	 * Sets the filter.
	 *
	 * @param filter the filter
	 * @return the search request builder
	 */
	public SearchRequestBuilder setFilter(Map filter) {
		sourceBuilder().filter(filter);
		return this;
	}

	/**
	 * Sets the min score.
	 *
	 * @param minScore the min score
	 * @return the search request builder
	 */
	public SearchRequestBuilder setMinScore(float minScore) {
		sourceBuilder().minScore(minScore);
		return this;
	}

	/**
	 * Sets the from.
	 *
	 * @param from the from
	 * @return the search request builder
	 */
	public SearchRequestBuilder setFrom(int from) {
		sourceBuilder().from(from);
		return this;
	}

	/**
	 * Sets the size.
	 *
	 * @param size the size
	 * @return the search request builder
	 */
	public SearchRequestBuilder setSize(int size) {
		sourceBuilder().size(size);
		return this;
	}

	/**
	 * Sets the explain.
	 *
	 * @param explain the explain
	 * @return the search request builder
	 */
	public SearchRequestBuilder setExplain(boolean explain) {
		sourceBuilder().explain(explain);
		return this;
	}

	/**
	 * Sets the version.
	 *
	 * @param version the version
	 * @return the search request builder
	 */
	public SearchRequestBuilder setVersion(boolean version) {
		sourceBuilder().version(version);
		return this;
	}

	/**
	 * Adds the index boost.
	 *
	 * @param index the index
	 * @param indexBoost the index boost
	 * @return the search request builder
	 */
	public SearchRequestBuilder addIndexBoost(String index, float indexBoost) {
		sourceBuilder().indexBoost(index, indexBoost);
		return this;
	}

	/**
	 * Sets the stats.
	 *
	 * @param statsGroups the stats groups
	 * @return the search request builder
	 */
	public SearchRequestBuilder setStats(String... statsGroups) {
		sourceBuilder().stats(statsGroups);
		return this;
	}

	/**
	 * Sets the no fields.
	 *
	 * @return the search request builder
	 */
	public SearchRequestBuilder setNoFields() {
		sourceBuilder().noFields();
		return this;
	}

	/**
	 * Adds the field.
	 *
	 * @param field the field
	 * @return the search request builder
	 */
	public SearchRequestBuilder addField(String field) {
		sourceBuilder().field(field);
		return this;
	}

	/**
	 * Adds the script field.
	 *
	 * @param name the name
	 * @param script the script
	 * @return the search request builder
	 */
	public SearchRequestBuilder addScriptField(String name, String script) {
		sourceBuilder().scriptField(name, script);
		return this;
	}

	/**
	 * Adds the script field.
	 *
	 * @param name the name
	 * @param script the script
	 * @param params the params
	 * @return the search request builder
	 */
	public SearchRequestBuilder addScriptField(String name, String script, Map<String, Object> params) {
		sourceBuilder().scriptField(name, script, params);
		return this;
	}

	/**
	 * Adds the partial field.
	 *
	 * @param name the name
	 * @param include the include
	 * @param exclude the exclude
	 * @return the search request builder
	 */
	public SearchRequestBuilder addPartialField(String name, @Nullable String include, @Nullable String exclude) {
		sourceBuilder().partialField(name, include, exclude);
		return this;
	}

	/**
	 * Adds the partial field.
	 *
	 * @param name the name
	 * @param includes the includes
	 * @param excludes the excludes
	 * @return the search request builder
	 */
	public SearchRequestBuilder addPartialField(String name, @Nullable String[] includes, @Nullable String[] excludes) {
		sourceBuilder().partialField(name, includes, excludes);
		return this;
	}

	/**
	 * Adds the script field.
	 *
	 * @param name the name
	 * @param lang the lang
	 * @param script the script
	 * @param params the params
	 * @return the search request builder
	 */
	public SearchRequestBuilder addScriptField(String name, String lang, String script, Map<String, Object> params) {
		sourceBuilder().scriptField(name, lang, script, params);
		return this;
	}

	/**
	 * Adds the sort.
	 *
	 * @param field the field
	 * @param order the order
	 * @return the search request builder
	 */
	public SearchRequestBuilder addSort(String field, SortOrder order) {
		sourceBuilder().sort(field, order);
		return this;
	}

	/**
	 * Adds the sort.
	 *
	 * @param sort the sort
	 * @return the search request builder
	 */
	public SearchRequestBuilder addSort(SortBuilder sort) {
		sourceBuilder().sort(sort);
		return this;
	}

	/**
	 * Sets the track scores.
	 *
	 * @param trackScores the track scores
	 * @return the search request builder
	 */
	public SearchRequestBuilder setTrackScores(boolean trackScores) {
		sourceBuilder().trackScores(trackScores);
		return this;
	}

	/**
	 * Adds the fields.
	 *
	 * @param fields the fields
	 * @return the search request builder
	 */
	public SearchRequestBuilder addFields(String... fields) {
		sourceBuilder().fields(fields);
		return this;
	}

	/**
	 * Adds the facet.
	 *
	 * @param facet the facet
	 * @return the search request builder
	 */
	public SearchRequestBuilder addFacet(AbstractFacetBuilder facet) {
		sourceBuilder().facet(facet);
		return this;
	}

	/**
	 * Sets the facets.
	 *
	 * @param facets the facets
	 * @return the search request builder
	 */
	public SearchRequestBuilder setFacets(byte[] facets) {
		sourceBuilder().facets(facets);
		return this;
	}

	/**
	 * Sets the facets.
	 *
	 * @param facets the facets
	 * @param facetsOffset the facets offset
	 * @param facetsLength the facets length
	 * @return the search request builder
	 */
	public SearchRequestBuilder setFacets(byte[] facets, int facetsOffset, int facetsLength) {
		sourceBuilder().facets(facets, facetsOffset, facetsLength);
		return this;
	}

	/**
	 * Sets the facets.
	 *
	 * @param facets the facets
	 * @return the search request builder
	 */
	public SearchRequestBuilder setFacets(XContentBuilder facets) {
		sourceBuilder().facets(facets);
		return this;
	}

	/**
	 * Sets the facets.
	 *
	 * @param facets the facets
	 * @return the search request builder
	 */
	public SearchRequestBuilder setFacets(Map facets) {
		sourceBuilder().facets(facets);
		return this;
	}

	/**
	 * Adds the highlighted field.
	 *
	 * @param name the name
	 * @return the search request builder
	 */
	public SearchRequestBuilder addHighlightedField(String name) {
		highlightBuilder().field(name);
		return this;
	}

	/**
	 * Adds the highlighted field.
	 *
	 * @param name the name
	 * @param fragmentSize the fragment size
	 * @return the search request builder
	 */
	public SearchRequestBuilder addHighlightedField(String name, int fragmentSize) {
		highlightBuilder().field(name, fragmentSize);
		return this;
	}

	/**
	 * Adds the highlighted field.
	 *
	 * @param name the name
	 * @param fragmentSize the fragment size
	 * @param numberOfFragments the number of fragments
	 * @return the search request builder
	 */
	public SearchRequestBuilder addHighlightedField(String name, int fragmentSize, int numberOfFragments) {
		highlightBuilder().field(name, fragmentSize, numberOfFragments);
		return this;
	}

	/**
	 * Adds the highlighted field.
	 *
	 * @param name the name
	 * @param fragmentSize the fragment size
	 * @param numberOfFragments the number of fragments
	 * @param fragmentOffset the fragment offset
	 * @return the search request builder
	 */
	public SearchRequestBuilder addHighlightedField(String name, int fragmentSize, int numberOfFragments,
			int fragmentOffset) {
		highlightBuilder().field(name, fragmentSize, numberOfFragments, fragmentOffset);
		return this;
	}

	/**
	 * Adds the highlighted field.
	 *
	 * @param field the field
	 * @return the search request builder
	 */
	public SearchRequestBuilder addHighlightedField(HighlightBuilder.Field field) {
		highlightBuilder().field(field);
		return this;
	}

	/**
	 * Sets the highlighter tags schema.
	 *
	 * @param schemaName the schema name
	 * @return the search request builder
	 */
	public SearchRequestBuilder setHighlighterTagsSchema(String schemaName) {
		highlightBuilder().tagsSchema(schemaName);
		return this;
	}

	/**
	 * Sets the highlighter pre tags.
	 *
	 * @param preTags the pre tags
	 * @return the search request builder
	 */
	public SearchRequestBuilder setHighlighterPreTags(String... preTags) {
		highlightBuilder().preTags(preTags);
		return this;
	}

	/**
	 * Sets the highlighter post tags.
	 *
	 * @param postTags the post tags
	 * @return the search request builder
	 */
	public SearchRequestBuilder setHighlighterPostTags(String... postTags) {
		highlightBuilder().postTags(postTags);
		return this;
	}

	/**
	 * Sets the highlighter order.
	 *
	 * @param order the order
	 * @return the search request builder
	 */
	public SearchRequestBuilder setHighlighterOrder(String order) {
		highlightBuilder().order(order);
		return this;
	}

	/**
	 * Sets the highlighter encoder.
	 *
	 * @param encoder the encoder
	 * @return the search request builder
	 */
	public SearchRequestBuilder setHighlighterEncoder(String encoder) {
		highlightBuilder().encoder(encoder);
		return this;
	}

	/**
	 * Sets the highlighter require field match.
	 *
	 * @param requireFieldMatch the require field match
	 * @return the search request builder
	 */
	public SearchRequestBuilder setHighlighterRequireFieldMatch(boolean requireFieldMatch) {
		highlightBuilder().requireFieldMatch(requireFieldMatch);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @return the search request builder
	 */
	public SearchRequestBuilder setSource(String source) {
		request.source(source);
		return this;
	}

	/**
	 * Sets the extra source.
	 *
	 * @param source the source
	 * @return the search request builder
	 */
	public SearchRequestBuilder setExtraSource(String source) {
		request.extraSource(source);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @return the search request builder
	 */
	public SearchRequestBuilder setSource(byte[] source) {
		request.source(source);
		return this;
	}

	/**
	 * Sets the extra source.
	 *
	 * @param source the source
	 * @return the search request builder
	 */
	public SearchRequestBuilder setExtraSource(byte[] source) {
		request.extraSource(source);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @return the search request builder
	 */
	public SearchRequestBuilder setSource(byte[] source, int offset, int length) {
		request.source(source, offset, length);
		return this;
	}

	/**
	 * Sets the extra source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @return the search request builder
	 */
	public SearchRequestBuilder setExtraSource(byte[] source, int offset, int length) {
		request.extraSource(source, offset, length);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param builder the builder
	 * @return the search request builder
	 */
	public SearchRequestBuilder setSource(XContentBuilder builder) {
		request.source(builder);
		return this;
	}

	/**
	 * Sets the extra source.
	 *
	 * @param builder the builder
	 * @return the search request builder
	 */
	public SearchRequestBuilder setExtraSource(XContentBuilder builder) {
		request.extraSource(builder);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @return the search request builder
	 */
	public SearchRequestBuilder setSource(Map source) {
		request.source(source);
		return this;
	}

	/**
	 * Sets the extra source.
	 *
	 * @param source the source
	 * @return the search request builder
	 */
	public SearchRequestBuilder setExtraSource(Map source) {
		request.extraSource(source);
		return this;
	}

	/**
	 * Internal builder.
	 *
	 * @param sourceBuilder the source builder
	 * @return the search request builder
	 */
	public SearchRequestBuilder internalBuilder(SearchSourceBuilder sourceBuilder) {
		this.sourceBuilder = sourceBuilder;
		return this;
	}

	/**
	 * Internal builder.
	 *
	 * @return the search source builder
	 */
	public SearchSourceBuilder internalBuilder() {
		return sourceBuilder();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return internalBuilder().toString();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.BaseRequestBuilder#request()
	 */
	@Override
	public SearchRequest request() {
		if (sourceBuilder != null) {
			request.source(sourceBuilder());
		}
		return request;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.BaseRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<SearchResponse> listener) {
		if (sourceBuilder != null) {
			request.source(sourceBuilder());
		}
		client.search(request, listener);
	}

	/**
	 * Source builder.
	 *
	 * @return the search source builder
	 */
	private SearchSourceBuilder sourceBuilder() {
		if (sourceBuilder == null) {
			sourceBuilder = new SearchSourceBuilder();
		}
		return sourceBuilder;
	}

	/**
	 * Highlight builder.
	 *
	 * @return the highlight builder
	 */
	private HighlightBuilder highlightBuilder() {
		return sourceBuilder().highlighter();
	}
}
