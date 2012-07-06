/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryFetchSearchResult.java 2012-7-6 14:29:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.search.SearchShardTarget;
import cn.com.rebirth.search.core.search.query.QuerySearchResult;
import cn.com.rebirth.search.core.search.query.QuerySearchResultProvider;

/**
 * The Class QueryFetchSearchResult.
 *
 * @author l.xue.nong
 */
public class QueryFetchSearchResult implements Streamable, QuerySearchResultProvider, FetchSearchResultProvider {

	/** The query result. */
	private QuerySearchResult queryResult;

	/** The fetch result. */
	private FetchSearchResult fetchResult;

	/**
	 * Instantiates a new query fetch search result.
	 */
	public QueryFetchSearchResult() {

	}

	/**
	 * Instantiates a new query fetch search result.
	 *
	 * @param queryResult the query result
	 * @param fetchResult the fetch result
	 */
	public QueryFetchSearchResult(QuerySearchResult queryResult, FetchSearchResult fetchResult) {
		this.queryResult = queryResult;
		this.fetchResult = fetchResult;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhaseResult#id()
	 */
	public long id() {
		return queryResult.id();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhaseResult#shardTarget()
	 */
	public SearchShardTarget shardTarget() {
		return queryResult.shardTarget();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhaseResult#shardTarget(cn.com.rebirth.search.core.search.SearchShardTarget)
	 */
	@Override
	public void shardTarget(SearchShardTarget shardTarget) {
		queryResult.shardTarget(shardTarget);
		fetchResult.shardTarget(shardTarget);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.query.QuerySearchResultProvider#includeFetch()
	 */
	@Override
	public boolean includeFetch() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.query.QuerySearchResultProvider#queryResult()
	 */
	public QuerySearchResult queryResult() {
		return queryResult;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSearchResultProvider#fetchResult()
	 */
	public FetchSearchResult fetchResult() {
		return fetchResult;
	}

	/**
	 * Read query fetch search result.
	 *
	 * @param in the in
	 * @return the query fetch search result
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static QueryFetchSearchResult readQueryFetchSearchResult(StreamInput in) throws IOException {
		QueryFetchSearchResult result = new QueryFetchSearchResult();
		result.readFrom(in);
		return result;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		queryResult = QuerySearchResult.readQuerySearchResult(in);
		fetchResult = FetchSearchResult.readFetchSearchResult(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		queryResult.writeTo(out);
		fetchResult.writeTo(out);
	}
}
