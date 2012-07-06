/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MoreLikeThisRequestBuilder.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.mlt;

import java.util.Map;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.search.SearchResponse;
import cn.com.rebirth.search.core.action.search.SearchType;
import cn.com.rebirth.search.core.action.support.BaseRequestBuilder;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.search.Scroll;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilder;

/**
 * The Class MoreLikeThisRequestBuilder.
 *
 * @author l.xue.nong
 */
public class MoreLikeThisRequestBuilder extends BaseRequestBuilder<MoreLikeThisRequest, SearchResponse> {

	/**
	 * Instantiates a new more like this request builder.
	 *
	 * @param client the client
	 */
	public MoreLikeThisRequestBuilder(Client client) {
		super(client, new MoreLikeThisRequest());
	}

	/**
	 * Instantiates a new more like this request builder.
	 *
	 * @param client the client
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 */
	public MoreLikeThisRequestBuilder(Client client, String index, String type, String id) {
		super(client, new MoreLikeThisRequest(index).type(type).id(id));
	}

	/**
	 * Sets the field.
	 *
	 * @param fields the fields
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setField(String... fields) {
		request.fields(fields);
		return this;
	}

	/**
	 * Sets the percent terms to match.
	 *
	 * @param percentTermsToMatch the percent terms to match
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setPercentTermsToMatch(float percentTermsToMatch) {
		request.percentTermsToMatch(percentTermsToMatch);
		return this;
	}

	/**
	 * Sets the min term freq.
	 *
	 * @param minTermFreq the min term freq
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setMinTermFreq(int minTermFreq) {
		request.minTermFreq(minTermFreq);
		return this;
	}

	/**
	 * Max query terms.
	 *
	 * @param maxQueryTerms the max query terms
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder maxQueryTerms(int maxQueryTerms) {
		request.maxQueryTerms(maxQueryTerms);
		return this;
	}

	/**
	 * Sets the stop words.
	 *
	 * @param stopWords the stop words
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setStopWords(String... stopWords) {
		request.stopWords(stopWords);
		return this;
	}

	/**
	 * Sets the min doc freq.
	 *
	 * @param minDocFreq the min doc freq
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setMinDocFreq(int minDocFreq) {
		request.minDocFreq(minDocFreq);
		return this;
	}

	/**
	 * Sets the max doc freq.
	 *
	 * @param maxDocFreq the max doc freq
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setMaxDocFreq(int maxDocFreq) {
		request.maxDocFreq(maxDocFreq);
		return this;
	}

	/**
	 * Sets the min word len.
	 *
	 * @param minWordLen the min word len
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setMinWordLen(int minWordLen) {
		request.minWordLen(minWordLen);
		return this;
	}

	/**
	 * Sets the max word len.
	 *
	 * @param maxWordLen the max word len
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setMaxWordLen(int maxWordLen) {
		request().maxWordLen(maxWordLen);
		return this;
	}

	/**
	 * Sets the boost terms.
	 *
	 * @param boostTerms the boost terms
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setBoostTerms(float boostTerms) {
		request.boostTerms(boostTerms);
		return this;
	}

	/**
	 * Sets the search source.
	 *
	 * @param sourceBuilder the source builder
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setSearchSource(SearchSourceBuilder sourceBuilder) {
		request.searchSource(sourceBuilder);
		return this;
	}

	/**
	 * Sets the search source.
	 *
	 * @param searchSource the search source
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setSearchSource(String searchSource) {
		request.searchSource(searchSource);
		return this;
	}

	/**
	 * Sets the search source.
	 *
	 * @param searchSource the search source
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setSearchSource(Map searchSource) {
		request.searchSource(searchSource);
		return this;
	}

	/**
	 * Sets the search source.
	 *
	 * @param builder the builder
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setSearchSource(XContentBuilder builder) {
		request.searchSource(builder);
		return this;
	}

	/**
	 * Sets the search source.
	 *
	 * @param searchSource the search source
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setSearchSource(byte[] searchSource) {
		request.searchSource(searchSource);
		return this;
	}

	/**
	 * Sets the search type.
	 *
	 * @param searchType the search type
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setSearchType(SearchType searchType) {
		request.searchType(searchType);
		return this;
	}

	/**
	 * Sets the search type.
	 *
	 * @param searchType the search type
	 * @return the more like this request builder
	 * @throws RebirthIllegalArgumentException the rebirth illegal argument exception
	 */
	public MoreLikeThisRequestBuilder setSearchType(String searchType) throws RebirthIllegalArgumentException {
		request.searchType(searchType);
		return this;
	}

	/**
	 * Sets the search indices.
	 *
	 * @param searchIndices the search indices
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setSearchIndices(String... searchIndices) {
		request.searchIndices(searchIndices);
		return this;
	}

	/**
	 * Sets the search types.
	 *
	 * @param searchTypes the search types
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setSearchTypes(String... searchTypes) {
		request.searchTypes(searchTypes);
		return this;
	}

	/**
	 * Sets the search scroll.
	 *
	 * @param searchScroll the search scroll
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setSearchScroll(Scroll searchScroll) {
		request.searchScroll(searchScroll);
		return this;
	}

	/**
	 * Sets the search size.
	 *
	 * @param size the size
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setSearchSize(int size) {
		request.searchSize(size);
		return this;
	}

	/**
	 * Sets the search from.
	 *
	 * @param from the from
	 * @return the more like this request builder
	 */
	public MoreLikeThisRequestBuilder setSearchFrom(int from) {
		request.searchFrom(from);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.BaseRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<SearchResponse> listener) {
		client.moreLikeThis(request, listener);
	}
}
