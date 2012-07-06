/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportSearchCache.java 2012-3-29 15:00:51 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.search.type;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;

import jsr166y.LinkedTransferQueue;
import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.search.core.search.SearchShardTarget;
import cn.com.rebirth.search.core.search.dfs.DfsSearchResult;
import cn.com.rebirth.search.core.search.fetch.FetchSearchResult;
import cn.com.rebirth.search.core.search.fetch.QueryFetchSearchResult;
import cn.com.rebirth.search.core.search.query.QuerySearchResultProvider;


/**
 * The Class TransportSearchCache.
 *
 * @author l.xue.nong
 */
public class TransportSearchCache {

	
	/** The cache dfs results. */
	private final Queue<Collection<DfsSearchResult>> cacheDfsResults = new LinkedTransferQueue<Collection<DfsSearchResult>>();

	
	/** The cache query results. */
	private final Queue<Map<SearchShardTarget, QuerySearchResultProvider>> cacheQueryResults = new LinkedTransferQueue<Map<SearchShardTarget, QuerySearchResultProvider>>();

	
	/** The cache fetch results. */
	private final Queue<Map<SearchShardTarget, FetchSearchResult>> cacheFetchResults = new LinkedTransferQueue<Map<SearchShardTarget, FetchSearchResult>>();

	
	/** The cache query fetch results. */
	private final Queue<Map<SearchShardTarget, QueryFetchSearchResult>> cacheQueryFetchResults = new LinkedTransferQueue<Map<SearchShardTarget, QueryFetchSearchResult>>();

	
	/**
	 * Obtain dfs results.
	 *
	 * @return the collection
	 */
	public Collection<DfsSearchResult> obtainDfsResults() {
		Collection<DfsSearchResult> dfsSearchResults;
		while ((dfsSearchResults = cacheDfsResults.poll()) == null) {
			cacheDfsResults.offer(new LinkedTransferQueue<DfsSearchResult>());
		}
		return dfsSearchResults;
	}

	
	/**
	 * Release dfs results.
	 *
	 * @param dfsResults the dfs results
	 */
	public void releaseDfsResults(Collection<DfsSearchResult> dfsResults) {
		dfsResults.clear();
		cacheDfsResults.offer(dfsResults);
	}

	
	/**
	 * Obtain query results.
	 *
	 * @return the map
	 */
	public Map<SearchShardTarget, QuerySearchResultProvider> obtainQueryResults() {
		Map<SearchShardTarget, QuerySearchResultProvider> queryResults;
		while ((queryResults = cacheQueryResults.poll()) == null) {
			cacheQueryResults.offer(ConcurrentCollections
					.<SearchShardTarget, QuerySearchResultProvider> newConcurrentMap());
		}
		return queryResults;
	}

	
	/**
	 * Release query results.
	 *
	 * @param queryResults the query results
	 */
	public void releaseQueryResults(Map<SearchShardTarget, QuerySearchResultProvider> queryResults) {
		queryResults.clear();
		cacheQueryResults.offer(queryResults);
	}

	
	/**
	 * Obtain fetch results.
	 *
	 * @return the map
	 */
	public Map<SearchShardTarget, FetchSearchResult> obtainFetchResults() {
		Map<SearchShardTarget, FetchSearchResult> fetchResults;
		while ((fetchResults = cacheFetchResults.poll()) == null) {
			cacheFetchResults.offer(ConcurrentCollections.<SearchShardTarget, FetchSearchResult> newConcurrentMap());
		}
		return fetchResults;
	}

	
	/**
	 * Release fetch results.
	 *
	 * @param fetchResults the fetch results
	 */
	public void releaseFetchResults(Map<SearchShardTarget, FetchSearchResult> fetchResults) {
		fetchResults.clear();
		cacheFetchResults.offer(fetchResults);
	}

	
	/**
	 * Obtain query fetch results.
	 *
	 * @return the map
	 */
	public Map<SearchShardTarget, QueryFetchSearchResult> obtainQueryFetchResults() {
		Map<SearchShardTarget, QueryFetchSearchResult> fetchResults;
		while ((fetchResults = cacheQueryFetchResults.poll()) == null) {
			cacheQueryFetchResults.offer(ConcurrentCollections
					.<SearchShardTarget, QueryFetchSearchResult> newConcurrentMap());
		}
		return fetchResults;
	}

	
	/**
	 * Release query fetch results.
	 *
	 * @param fetchResults the fetch results
	 */
	public void releaseQueryFetchResults(Map<SearchShardTarget, QueryFetchSearchResult> fetchResults) {
		fetchResults.clear();
		cacheQueryFetchResults.offer(fetchResults);
	}
}
