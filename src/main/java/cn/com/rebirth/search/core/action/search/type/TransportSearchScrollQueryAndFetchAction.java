/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportSearchScrollQueryAndFetchAction.java 2012-7-6 14:29:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search.type;

import static cn.com.rebirth.search.core.action.search.type.TransportSearchHelper.internalScrollSearchRequest;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jsr166y.LinkedTransferQueue;
import cn.com.rebirth.commons.collect.Tuple;
import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.search.ReduceSearchPhaseException;
import cn.com.rebirth.search.core.action.search.SearchOperationThreading;
import cn.com.rebirth.search.core.action.search.SearchPhaseExecutionException;
import cn.com.rebirth.search.core.action.search.SearchResponse;
import cn.com.rebirth.search.core.action.search.SearchScrollRequest;
import cn.com.rebirth.search.core.action.search.ShardSearchFailure;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.search.SearchShardTarget;
import cn.com.rebirth.search.core.search.action.SearchServiceListener;
import cn.com.rebirth.search.core.search.action.SearchServiceTransportAction;
import cn.com.rebirth.search.core.search.controller.SearchPhaseController;
import cn.com.rebirth.search.core.search.controller.ShardDoc;
import cn.com.rebirth.search.core.search.fetch.QueryFetchSearchResult;
import cn.com.rebirth.search.core.search.internal.InternalSearchResponse;

/**
 * The Class TransportSearchScrollQueryAndFetchAction.
 *
 * @author l.xue.nong
 */
public class TransportSearchScrollQueryAndFetchAction extends AbstractComponent {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The search service. */
	private final SearchServiceTransportAction searchService;

	/** The search phase controller. */
	private final SearchPhaseController searchPhaseController;

	/** The search cache. */
	private final TransportSearchCache searchCache;

	/**
	 * Instantiates a new transport search scroll query and fetch action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param searchCache the search cache
	 * @param searchService the search service
	 * @param searchPhaseController the search phase controller
	 */
	@Inject
	public TransportSearchScrollQueryAndFetchAction(Settings settings, ThreadPool threadPool,
			ClusterService clusterService, TransportSearchCache searchCache,
			SearchServiceTransportAction searchService, SearchPhaseController searchPhaseController) {
		super(settings);
		this.threadPool = threadPool;
		this.clusterService = clusterService;
		this.searchCache = searchCache;
		this.searchService = searchService;
		this.searchPhaseController = searchPhaseController;
	}

	/**
	 * Execute.
	 *
	 * @param request the request
	 * @param scrollId the scroll id
	 * @param listener the listener
	 */
	public void execute(SearchScrollRequest request, ParsedScrollId scrollId, ActionListener<SearchResponse> listener) {
		new AsyncAction(request, scrollId, listener).start();
	}

	/**
	 * The Class AsyncAction.
	 *
	 * @author l.xue.nong
	 */
	private class AsyncAction {

		/** The request. */
		private final SearchScrollRequest request;

		/** The listener. */
		private final ActionListener<SearchResponse> listener;

		/** The scroll id. */
		private final ParsedScrollId scrollId;

		/** The nodes. */
		private final DiscoveryNodes nodes;

		/** The shard failures. */
		private volatile LinkedTransferQueue<ShardSearchFailure> shardFailures;

		/** The query fetch results. */
		private final Map<SearchShardTarget, QueryFetchSearchResult> queryFetchResults = searchCache
				.obtainQueryFetchResults();

		/** The successful ops. */
		private final AtomicInteger successfulOps;

		/** The counter. */
		private final AtomicInteger counter;

		/** The start time. */
		private final long startTime = System.currentTimeMillis();

		/**
		 * Instantiates a new async action.
		 *
		 * @param request the request
		 * @param scrollId the scroll id
		 * @param listener the listener
		 */
		private AsyncAction(SearchScrollRequest request, ParsedScrollId scrollId,
				ActionListener<SearchResponse> listener) {
			this.request = request;
			this.listener = listener;
			this.scrollId = scrollId;
			this.nodes = clusterService.state().nodes();
			this.successfulOps = new AtomicInteger(scrollId.context().length);
			this.counter = new AtomicInteger(scrollId.context().length);
		}

		/**
		 * Builds the shard failures.
		 *
		 * @return the shard search failure[]
		 */
		protected final ShardSearchFailure[] buildShardFailures() {
			LinkedTransferQueue<ShardSearchFailure> localFailures = shardFailures;
			if (localFailures == null) {
				return ShardSearchFailure.EMPTY_ARRAY;
			}
			return localFailures.toArray(ShardSearchFailure.EMPTY_ARRAY);
		}

		/**
		 * Adds the shard failure.
		 *
		 * @param failure the failure
		 */
		protected final void addShardFailure(ShardSearchFailure failure) {
			if (shardFailures == null) {
				shardFailures = new LinkedTransferQueue<ShardSearchFailure>();
			}
			shardFailures.add(failure);
		}

		/**
		 * Start.
		 */
		public void start() {
			if (scrollId.context().length == 0) {
				listener.onFailure(new SearchPhaseExecutionException("query", "no nodes to search on", null));
				return;
			}

			int localOperations = 0;
			for (Tuple<String, Long> target : scrollId.context()) {
				DiscoveryNode node = nodes.get(target.v1());
				if (node != null) {
					if (nodes.localNodeId().equals(node.id())) {
						localOperations++;
					} else {
						executePhase(node, target.v2());
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Node [" + target.v1() + "] not available for scroll request ["
								+ scrollId.source() + "]");
					}
					successfulOps.decrementAndGet();
					if (counter.decrementAndGet() == 0) {
						finishHim();
					}
				}
			}

			if (localOperations > 0) {
				if (request.operationThreading() == SearchOperationThreading.SINGLE_THREAD) {
					threadPool.executor(ThreadPool.Names.SEARCH).execute(new Runnable() {
						@Override
						public void run() {
							for (Tuple<String, Long> target : scrollId.context()) {
								DiscoveryNode node = nodes.get(target.v1());
								if (node != null && nodes.localNodeId().equals(node.id())) {
									executePhase(node, target.v2());
								}
							}
						}
					});
				} else {
					boolean localAsync = request.operationThreading() == SearchOperationThreading.THREAD_PER_SHARD;
					for (final Tuple<String, Long> target : scrollId.context()) {
						final DiscoveryNode node = nodes.get(target.v1());
						if (node != null && nodes.localNodeId().equals(node.id())) {
							if (localAsync) {
								threadPool.executor(ThreadPool.Names.SEARCH).execute(new Runnable() {
									@Override
									public void run() {
										executePhase(node, target.v2());
									}
								});
							} else {
								executePhase(node, target.v2());
							}
						}
					}
				}
			}

			for (Tuple<String, Long> target : scrollId.context()) {
				DiscoveryNode node = nodes.get(target.v1());
				if (node == null) {
					if (logger.isDebugEnabled()) {
						logger.debug("Node [" + target.v1() + "] not available for scroll request ["
								+ scrollId.source() + "]");
					}
					successfulOps.decrementAndGet();
					if (counter.decrementAndGet() == 0) {
						finishHim();
					}
				} else {
				}
			}
		}

		/**
		 * Execute phase.
		 *
		 * @param node the node
		 * @param searchId the search id
		 */
		private void executePhase(DiscoveryNode node, final long searchId) {
			searchService.sendExecuteFetch(node, internalScrollSearchRequest(searchId, request),
					new SearchServiceListener<QueryFetchSearchResult>() {
						@Override
						public void onResult(QueryFetchSearchResult result) {
							queryFetchResults.put(result.shardTarget(), result);
							if (counter.decrementAndGet() == 0) {
								finishHim();
							}
						}

						@Override
						public void onFailure(Throwable t) {
							if (logger.isDebugEnabled()) {
								logger.debug("[{}] Failed to execute query phase", t, searchId);
							}
							addShardFailure(new ShardSearchFailure(t));
							successfulOps.decrementAndGet();
							if (counter.decrementAndGet() == 0) {
								finishHim();
							}
						}
					});
		}

		/**
		 * Finish him.
		 */
		private void finishHim() {
			try {
				innerFinishHim();
			} catch (Exception e) {
				listener.onFailure(new ReduceSearchPhaseException("fetch", "", e, buildShardFailures()));
			}
		}

		/**
		 * Inner finish him.
		 */
		private void innerFinishHim() {
			ShardDoc[] sortedShardList = searchPhaseController.sortDocs(queryFetchResults.values());
			final InternalSearchResponse internalResponse = searchPhaseController.merge(sortedShardList,
					queryFetchResults, queryFetchResults);
			String scrollId = null;
			if (request.scroll() != null) {
				scrollId = request.scrollId();
			}
			searchCache.releaseQueryFetchResults(queryFetchResults);
			listener.onResponse(new SearchResponse(internalResponse, scrollId, this.scrollId.context().length,
					successfulOps.get(), System.currentTimeMillis() - startTime, buildShardFailures()));
		}
	}
}