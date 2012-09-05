/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportSearchQueryThenFetchAction.java 2012-7-6 14:29:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search.type;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.trove.ExtTIntArrayList;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.search.ReduceSearchPhaseException;
import cn.com.rebirth.search.core.action.search.SearchOperationThreading;
import cn.com.rebirth.search.core.action.search.SearchRequest;
import cn.com.rebirth.search.core.action.search.SearchResponse;
import cn.com.rebirth.search.core.action.search.ShardSearchFailure;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.search.SearchShardTarget;
import cn.com.rebirth.search.core.search.action.SearchServiceListener;
import cn.com.rebirth.search.core.search.action.SearchServiceTransportAction;
import cn.com.rebirth.search.core.search.controller.SearchPhaseController;
import cn.com.rebirth.search.core.search.fetch.FetchSearchRequest;
import cn.com.rebirth.search.core.search.fetch.FetchSearchResult;
import cn.com.rebirth.search.core.search.internal.InternalSearchRequest;
import cn.com.rebirth.search.core.search.internal.InternalSearchResponse;
import cn.com.rebirth.search.core.search.query.QuerySearchResult;
import cn.com.rebirth.search.core.search.query.QuerySearchResultProvider;

/**
 * The Class TransportSearchQueryThenFetchAction.
 *
 * @author l.xue.nong
 */
public class TransportSearchQueryThenFetchAction extends TransportSearchTypeAction {

	/**
	 * Instantiates a new transport search query then fetch action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportSearchCache the transport search cache
	 * @param searchService the search service
	 * @param searchPhaseController the search phase controller
	 */
	@Inject
	public TransportSearchQueryThenFetchAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
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

		/** The query results. */
		private final Map<SearchShardTarget, QuerySearchResultProvider> queryResults = searchCache.obtainQueryResults();

		/** The fetch results. */
		private final Map<SearchShardTarget, FetchSearchResult> fetchResults = searchCache.obtainFetchResults();

		/** The doc ids to load. */
		private volatile Map<SearchShardTarget, ExtTIntArrayList> docIdsToLoad;

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
			queryResults.put(result.shardTarget(), result);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.search.type.TransportSearchTypeAction.BaseAsyncAction#moveToSecondPhase()
		 */
		@Override
		protected void moveToSecondPhase() {
			sortedShardList = searchPhaseController.sortDocs(queryResults.values());
			final Map<SearchShardTarget, ExtTIntArrayList> docIdsToLoad = searchPhaseController
					.docIdsToLoad(sortedShardList);
			this.docIdsToLoad = docIdsToLoad;

			if (docIdsToLoad.isEmpty()) {
				finishHim();
				return;
			}

			final AtomicInteger counter = new AtomicInteger(docIdsToLoad.size());

			int localOperations = 0;
			for (final Map.Entry<SearchShardTarget, ExtTIntArrayList> entry : docIdsToLoad.entrySet()) {
				DiscoveryNode node = nodes.get(entry.getKey().nodeId());
				if (node.id().equals(nodes.localNodeId())) {
					localOperations++;
				} else {
					FetchSearchRequest fetchSearchRequest = new FetchSearchRequest(queryResults.get(entry.getKey())
							.id(), entry.getValue());
					executeFetch(entry.getKey(), counter, fetchSearchRequest, node);
				}
			}

			if (localOperations > 0) {
				if (request.operationThreading() == SearchOperationThreading.SINGLE_THREAD) {
					threadPool.executor(ThreadPool.Names.SEARCH).execute(new Runnable() {
						@Override
						public void run() {
							for (final Map.Entry<SearchShardTarget, ExtTIntArrayList> entry : docIdsToLoad.entrySet()) {
								DiscoveryNode node = nodes.get(entry.getKey().nodeId());
								if (node.id().equals(nodes.localNodeId())) {
									FetchSearchRequest fetchSearchRequest = new FetchSearchRequest(queryResults.get(
											entry.getKey()).id(), entry.getValue());
									executeFetch(entry.getKey(), counter, fetchSearchRequest, node);
								}
							}
						}
					});
				} else {
					boolean localAsync = request.operationThreading() == SearchOperationThreading.THREAD_PER_SHARD;
					for (final Map.Entry<SearchShardTarget, ExtTIntArrayList> entry : docIdsToLoad.entrySet()) {
						final DiscoveryNode node = nodes.get(entry.getKey().nodeId());
						if (node.id().equals(nodes.localNodeId())) {
							final FetchSearchRequest fetchSearchRequest = new FetchSearchRequest(queryResults.get(
									entry.getKey()).id(), entry.getValue());
							if (localAsync) {
								threadPool.executor(ThreadPool.Names.SEARCH).execute(new Runnable() {
									@Override
									public void run() {
										executeFetch(entry.getKey(), counter, fetchSearchRequest, node);
									}
								});
							} else {
								executeFetch(entry.getKey(), counter, fetchSearchRequest, node);
							}
						}
					}
				}
			}
		}

		/**
		 * Execute fetch.
		 *
		 * @param shardTarget the shard target
		 * @param counter the counter
		 * @param fetchSearchRequest the fetch search request
		 * @param node the node
		 */
		void executeFetch(final SearchShardTarget shardTarget, final AtomicInteger counter,
				final FetchSearchRequest fetchSearchRequest, DiscoveryNode node) {
			searchService.sendExecuteFetch(node, fetchSearchRequest, new SearchServiceListener<FetchSearchResult>() {
				@Override
				public void onResult(FetchSearchResult result) {
					result.shardTarget(shardTarget);
					fetchResults.put(result.shardTarget(), result);
					if (counter.decrementAndGet() == 0) {
						finishHim();
					}
				}

				@Override
				public void onFailure(Throwable t) {
					if (logger.isDebugEnabled()) {
						logger.debug("[{}] Failed to execute fetch phase", t, fetchSearchRequest.id());
					}
					AsyncAction.this.addShardFailure(new ShardSearchFailure(t));
					successulOps.decrementAndGet();
					if (counter.decrementAndGet() == 0) {
						finishHim();
					}
				}
			});
		}

		/**
		 * Finish him.
		 */
		void finishHim() {
			try {
				innerFinishHim();
			} catch (Exception e) {
				ReduceSearchPhaseException failure = new ReduceSearchPhaseException("fetch", "", e,
						buildShardFailures());
				if (logger.isDebugEnabled()) {
					logger.debug("failed to reduce search", failure);
				}
				listener.onFailure(failure);
			} finally {
				releaseIrrelevantSearchContexts(queryResults, docIdsToLoad);
				searchCache.releaseQueryResults(queryResults);
				searchCache.releaseFetchResults(fetchResults);
			}
		}

		/**
		 * Inner finish him.
		 *
		 * @throws Exception the exception
		 */
		void innerFinishHim() throws Exception {
			InternalSearchResponse internalResponse = searchPhaseController.merge(sortedShardList, queryResults,
					fetchResults);
			String scrollId = null;
			if (request.scroll() != null) {
				scrollId = TransportSearchHelper.buildScrollId(request.searchType(), queryResults.values(), null);
			}
			listener.onResponse(new SearchResponse(internalResponse, scrollId, expectedSuccessfulOps, successulOps
					.get(), buildTookInMillis(), buildShardFailures()));
		}
	}
}