/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportSearchDfsQueryAndFetchAction.java 2012-7-6 14:30:05 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search.type;

import static cn.com.rebirth.search.core.action.search.type.TransportSearchHelper.buildScrollId;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.rebirth.commons.settings.Settings;
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
import cn.com.rebirth.search.core.search.dfs.AggregatedDfs;
import cn.com.rebirth.search.core.search.dfs.DfsSearchResult;
import cn.com.rebirth.search.core.search.fetch.QueryFetchSearchResult;
import cn.com.rebirth.search.core.search.internal.InternalSearchRequest;
import cn.com.rebirth.search.core.search.internal.InternalSearchResponse;
import cn.com.rebirth.search.core.search.query.QuerySearchRequest;

/**
 * The Class TransportSearchDfsQueryAndFetchAction.
 *
 * @author l.xue.nong
 */
public class TransportSearchDfsQueryAndFetchAction extends TransportSearchTypeAction {

	/**
	 * Instantiates a new transport search dfs query and fetch action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportSearchCache the transport search cache
	 * @param searchService the search service
	 * @param searchPhaseController the search phase controller
	 */
	@Inject
	public TransportSearchDfsQueryAndFetchAction(Settings settings, ThreadPool threadPool,
			ClusterService clusterService, TransportSearchCache transportSearchCache,
			SearchServiceTransportAction searchService, SearchPhaseController searchPhaseController) {
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
	private class AsyncAction extends BaseAsyncAction<DfsSearchResult> {

		/** The dfs results. */
		private final Collection<DfsSearchResult> dfsResults = searchCache.obtainDfsResults();

		/** The query fetch results. */
		private final Map<SearchShardTarget, QueryFetchSearchResult> queryFetchResults = searchCache
				.obtainQueryFetchResults();

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
			return "dfs";
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.search.type.TransportSearchTypeAction.BaseAsyncAction#sendExecuteFirstPhase(cn.com.rebirth.search.core.cluster.node.DiscoveryNode, cn.com.rebirth.search.core.search.internal.InternalSearchRequest, cn.com.rebirth.search.core.search.action.SearchServiceListener)
		 */
		@Override
		protected void sendExecuteFirstPhase(DiscoveryNode node, InternalSearchRequest request,
				SearchServiceListener<DfsSearchResult> listener) {
			searchService.sendExecuteDfs(node, request, listener);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.search.type.TransportSearchTypeAction.BaseAsyncAction#processFirstPhaseResult(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.search.SearchPhaseResult)
		 */
		@Override
		protected void processFirstPhaseResult(ShardRouting shard, DfsSearchResult result) {
			dfsResults.add(result);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.search.type.TransportSearchTypeAction.BaseAsyncAction#moveToSecondPhase()
		 */
		@Override
		protected void moveToSecondPhase() {
			final AggregatedDfs dfs = searchPhaseController.aggregateDfs(dfsResults);
			final AtomicInteger counter = new AtomicInteger(dfsResults.size());

			int localOperations = 0;
			for (final DfsSearchResult dfsResult : dfsResults) {
				DiscoveryNode node = nodes.get(dfsResult.shardTarget().nodeId());
				if (node.id().equals(nodes.localNodeId())) {
					localOperations++;
				} else {
					QuerySearchRequest querySearchRequest = new QuerySearchRequest(dfsResult.id(), dfs);
					executeSecondPhase(dfsResult, counter, node, querySearchRequest);
				}
			}
			if (localOperations > 0) {
				if (request.operationThreading() == SearchOperationThreading.SINGLE_THREAD) {
					threadPool.executor(ThreadPool.Names.SEARCH).execute(new Runnable() {
						@Override
						public void run() {
							for (final DfsSearchResult dfsResult : dfsResults) {
								DiscoveryNode node = nodes.get(dfsResult.shardTarget().nodeId());
								if (node.id().equals(nodes.localNodeId())) {
									QuerySearchRequest querySearchRequest = new QuerySearchRequest(dfsResult.id(), dfs);
									executeSecondPhase(dfsResult, counter, node, querySearchRequest);
								}
							}
						}
					});
				} else {
					boolean localAsync = request.operationThreading() == SearchOperationThreading.THREAD_PER_SHARD;
					for (final DfsSearchResult dfsResult : dfsResults) {
						final DiscoveryNode node = nodes.get(dfsResult.shardTarget().nodeId());
						if (node.id().equals(nodes.localNodeId())) {
							final QuerySearchRequest querySearchRequest = new QuerySearchRequest(dfsResult.id(), dfs);
							if (localAsync) {
								threadPool.executor(ThreadPool.Names.SEARCH).execute(new Runnable() {
									@Override
									public void run() {
										executeSecondPhase(dfsResult, counter, node, querySearchRequest);
									}
								});
							} else {
								executeSecondPhase(dfsResult, counter, node, querySearchRequest);
							}
						}
					}
				}
			}
		}

		/**
		 * Execute second phase.
		 *
		 * @param dfsResult the dfs result
		 * @param counter the counter
		 * @param node the node
		 * @param querySearchRequest the query search request
		 */
		void executeSecondPhase(final DfsSearchResult dfsResult, final AtomicInteger counter, DiscoveryNode node,
				final QuerySearchRequest querySearchRequest) {
			searchService.sendExecuteFetch(node, querySearchRequest,
					new SearchServiceListener<QueryFetchSearchResult>() {
						@Override
						public void onResult(QueryFetchSearchResult result) {
							result.shardTarget(dfsResult.shardTarget());
							queryFetchResults.put(result.shardTarget(), result);
							if (counter.decrementAndGet() == 0) {
								finishHim();
							}
						}

						@Override
						public void onFailure(Throwable t) {
							if (logger.isDebugEnabled()) {
								logger.debug("[{}] Failed to execute query phase", t, querySearchRequest.id());
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
				ReduceSearchPhaseException failure = new ReduceSearchPhaseException("query_fetch", "", e,
						buildShardFailures());
				if (logger.isDebugEnabled()) {
					logger.debug("failed to reduce search", failure);
				}
				listener.onFailure(failure);
			} finally {
				searchCache.releaseDfsResults(dfsResults);
				searchCache.releaseQueryFetchResults(queryFetchResults);
			}
		}

		/**
		 * Inner finish him.
		 *
		 * @throws Exception the exception
		 */
		void innerFinishHim() throws Exception {
			sortedShardList = searchPhaseController.sortDocs(queryFetchResults.values());
			final InternalSearchResponse internalResponse = searchPhaseController.merge(sortedShardList,
					queryFetchResults, queryFetchResults);
			String scrollId = null;
			if (request.scroll() != null) {
				scrollId = buildScrollId(request.searchType(), dfsResults, null);
			}
			listener.onResponse(new SearchResponse(internalResponse, scrollId, expectedSuccessfulOps, successulOps
					.get(), buildTookInMillis(), buildShardFailures()));
		}
	}
}