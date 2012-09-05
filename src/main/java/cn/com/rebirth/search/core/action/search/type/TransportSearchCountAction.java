/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportSearchCountAction.java 2012-7-6 14:30:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search.type;

import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.search.SearchRequest;
import cn.com.rebirth.search.core.action.search.SearchResponse;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.search.SearchShardTarget;
import cn.com.rebirth.search.core.search.action.SearchServiceListener;
import cn.com.rebirth.search.core.search.action.SearchServiceTransportAction;
import cn.com.rebirth.search.core.search.controller.SearchPhaseController;
import cn.com.rebirth.search.core.search.controller.ShardDoc;
import cn.com.rebirth.search.core.search.fetch.FetchSearchResultProvider;
import cn.com.rebirth.search.core.search.internal.InternalSearchRequest;
import cn.com.rebirth.search.core.search.internal.InternalSearchResponse;
import cn.com.rebirth.search.core.search.query.QuerySearchResult;
import cn.com.rebirth.search.core.search.query.QuerySearchResultProvider;

import com.google.common.collect.ImmutableMap;

/**
 * The Class TransportSearchCountAction.
 *
 * @author l.xue.nong
 */
public class TransportSearchCountAction extends TransportSearchTypeAction {

	/**
	 * Instantiates a new transport search count action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportSearchCache the transport search cache
	 * @param searchService the search service
	 * @param searchPhaseController the search phase controller
	 */
	@Inject
	public TransportSearchCountAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			TransportSearchCache transportSearchCache, SearchServiceTransportAction searchService,
			SearchPhaseController searchPhaseController) {
		super(settings, threadPool, clusterService, transportSearchCache, searchService, searchPhaseController);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.TransportAction#doExecute(cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(SearchRequest searchRequest, ActionListener<SearchResponse> listener) {
		new AsyncAction(searchRequest, listener).start();
	}

	/**
	 * The Class AsyncAction.
	 *
	 * @author l.xue.nong
	 */
	private class AsyncAction extends BaseAsyncAction<QuerySearchResult> {

		/** The query fetch results. */
		private final Map<SearchShardTarget, QuerySearchResultProvider> queryFetchResults = searchCache
				.obtainQueryResults();

		/**
		 * Instantiates a new async action.
		 *
		 * @param request the request
		 * @param listener the listener
		 */
		private AsyncAction(SearchRequest request, ActionListener<SearchResponse> listener) {
			super(request, listener);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.search.type.TransportSearchTypeAction.BaseAsyncAction#firstPhaseName()
		 */
		@Override
		protected String firstPhaseName() {
			return "query";
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.search.type.TransportSearchTypeAction.BaseAsyncAction#sendExecuteFirstPhase(cn.com.rebirth.search.core.cluster.node.DiscoveryNode, cn.com.rebirth.search.core.search.internal.InternalSearchRequest, cn.com.rebirth.search.core.search.action.SearchServiceListener)
		 */
		@Override
		protected void sendExecuteFirstPhase(DiscoveryNode node, InternalSearchRequest request,
				SearchServiceListener<QuerySearchResult> listener) {
			searchService.sendExecuteQuery(node, request, listener);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.search.type.TransportSearchTypeAction.BaseAsyncAction#processFirstPhaseResult(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.search.SearchPhaseResult)
		 */
		@Override
		protected void processFirstPhaseResult(ShardRouting shard, QuerySearchResult result) {
			queryFetchResults.put(result.shardTarget(), result);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.search.type.TransportSearchTypeAction.BaseAsyncAction#moveToSecondPhase()
		 */
		@Override
		protected void moveToSecondPhase() throws Exception {

			final InternalSearchResponse internalResponse = searchPhaseController.merge(EMPTY_DOCS, queryFetchResults,
					ImmutableMap.<SearchShardTarget, FetchSearchResultProvider> of());
			String scrollId = null;
			if (request.scroll() != null) {
				scrollId = TransportSearchHelper.buildScrollId(request.searchType(), queryFetchResults.values(), null);
			}
			listener.onResponse(new SearchResponse(internalResponse, scrollId, expectedSuccessfulOps, successulOps
					.get(), buildTookInMillis(), buildShardFailures()));
			searchCache.releaseQueryResults(queryFetchResults);
		}
	}

	/** The empty docs. */
	private static ShardDoc[] EMPTY_DOCS = new ShardDoc[0];
}