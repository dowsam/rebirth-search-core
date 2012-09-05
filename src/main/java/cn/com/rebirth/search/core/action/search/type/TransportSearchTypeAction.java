/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportSearchTypeAction.java 2012-7-6 14:30:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search.type;

import static cn.com.rebirth.search.core.action.search.type.TransportSearchHelper.internalSearchRequest;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jsr166y.LinkedTransferQueue;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.trove.ExtTIntArrayList;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.search.ReduceSearchPhaseException;
import cn.com.rebirth.search.core.action.search.SearchOperationThreading;
import cn.com.rebirth.search.core.action.search.SearchPhaseExecutionException;
import cn.com.rebirth.search.core.action.search.SearchRequest;
import cn.com.rebirth.search.core.action.search.SearchResponse;
import cn.com.rebirth.search.core.action.search.ShardSearchFailure;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.GroupShardsIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.search.SearchPhaseResult;
import cn.com.rebirth.search.core.search.SearchShardTarget;
import cn.com.rebirth.search.core.search.action.SearchServiceListener;
import cn.com.rebirth.search.core.search.action.SearchServiceTransportAction;
import cn.com.rebirth.search.core.search.controller.SearchPhaseController;
import cn.com.rebirth.search.core.search.controller.ShardDoc;
import cn.com.rebirth.search.core.search.internal.InternalSearchRequest;
import cn.com.rebirth.search.core.search.query.QuerySearchResultProvider;

/**
 * The Class TransportSearchTypeAction.
 *
 * @author l.xue.nong
 */
public abstract class TransportSearchTypeAction extends TransportAction<SearchRequest, SearchResponse> {

	/** The cluster service. */
	protected final ClusterService clusterService;

	/** The search service. */
	protected final SearchServiceTransportAction searchService;

	/** The search phase controller. */
	protected final SearchPhaseController searchPhaseController;

	/** The search cache. */
	protected final TransportSearchCache searchCache;

	/**
	 * Instantiates a new transport search type action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param searchCache the search cache
	 * @param searchService the search service
	 * @param searchPhaseController the search phase controller
	 */
	public TransportSearchTypeAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			TransportSearchCache searchCache, SearchServiceTransportAction searchService,
			SearchPhaseController searchPhaseController) {
		super(settings, threadPool);
		this.clusterService = clusterService;
		this.searchCache = searchCache;
		this.searchService = searchService;
		this.searchPhaseController = searchPhaseController;
	}

	/**
	 * The Class BaseAsyncAction.
	 *
	 * @param <FirstResult> the generic type
	 * @author l.xue.nong
	 */
	protected abstract class BaseAsyncAction<FirstResult extends SearchPhaseResult> {

		/** The listener. */
		protected final ActionListener<SearchResponse> listener;

		/** The shards its. */
		private final GroupShardsIterator shardsIts;

		/** The request. */
		protected final SearchRequest request;

		/** The cluster state. */
		protected final ClusterState clusterState;

		/** The nodes. */
		protected final DiscoveryNodes nodes;

		/** The expected successful ops. */
		protected final int expectedSuccessfulOps;

		/** The expected total ops. */
		private final int expectedTotalOps;

		/** The successul ops. */
		protected final AtomicInteger successulOps = new AtomicInteger();

		/** The total ops. */
		private final AtomicInteger totalOps = new AtomicInteger();

		/** The shard failures. */
		private volatile LinkedTransferQueue<ShardSearchFailure> shardFailures;

		/** The sorted shard list. */
		protected volatile ShardDoc[] sortedShardList;

		/** The start time. */
		protected final long startTime = System.currentTimeMillis();

		/**
		 * Instantiates a new base async action.
		 *
		 * @param request the request
		 * @param listener the listener
		 */
		protected BaseAsyncAction(SearchRequest request, ActionListener<SearchResponse> listener) {
			this.request = request;
			this.listener = listener;

			this.clusterState = clusterService.state();
			nodes = clusterState.nodes();

			clusterState.blocks().globalBlockedRaiseException(ClusterBlockLevel.READ);

			String[] concreteIndices = clusterState.metaData().concreteIndices(request.indices(), false, true);

			for (String index : concreteIndices) {
				clusterState.blocks().indexBlockedRaiseException(ClusterBlockLevel.READ, index);
			}

			Map<String, Set<String>> routingMap = clusterState.metaData().resolveSearchRouting(request.routing(),
					request.indices());

			shardsIts = clusterService.operationRouting().searchShards(clusterState, request.indices(),
					concreteIndices, request.queryHint(), routingMap, request.preference());
			expectedSuccessfulOps = shardsIts.size();

			expectedTotalOps = shardsIts.totalSizeWith1ForEmpty();

			if (expectedSuccessfulOps == 0) {

				throw new SearchPhaseExecutionException(
						"initial",
						"No indices / shards to search on, requested indices are " + Arrays.toString(request.indices()),
						buildShardFailures());
			}
		}

		/**
		 * Start.
		 */
		public void start() {
			request.beforeStart();

			int localOperations = 0;
			for (final ShardIterator shardIt : shardsIts) {
				final ShardRouting shard = shardIt.firstOrNull();
				if (shard != null) {
					if (shard.currentNodeId().equals(nodes.localNodeId())) {
						localOperations++;
					} else {

						performFirstPhase(shardIt);
					}
				} else {

					onFirstPhaseResult(null, shardIt, null);
				}
			}

			if (localOperations > 0) {
				if (request.operationThreading() == SearchOperationThreading.SINGLE_THREAD) {
					request.beforeLocalFork();
					threadPool.executor(ThreadPool.Names.SEARCH).execute(new Runnable() {
						@Override
						public void run() {
							for (final ShardIterator shardIt : shardsIts) {
								final ShardRouting shard = shardIt.firstOrNull();
								if (shard != null) {
									if (shard.currentNodeId().equals(nodes.localNodeId())) {
										performFirstPhase(shardIt);
									}
								}
							}
						}
					});
				} else {
					boolean localAsync = request.operationThreading() == SearchOperationThreading.THREAD_PER_SHARD;
					if (localAsync) {
						request.beforeLocalFork();
					}
					for (final ShardIterator shardIt : shardsIts) {
						final ShardRouting shard = shardIt.firstOrNull();
						if (shard != null) {
							if (shard.currentNodeId().equals(nodes.localNodeId())) {
								if (localAsync) {
									threadPool.executor(ThreadPool.Names.SEARCH).execute(new Runnable() {
										@Override
										public void run() {
											performFirstPhase(shardIt);
										}
									});
								} else {
									performFirstPhase(shardIt);
								}
							}
						}
					}
				}
			}
		}

		/**
		 * Perform first phase.
		 *
		 * @param shardIt the shard it
		 */
		void performFirstPhase(final ShardIterator shardIt) {
			performFirstPhase(shardIt, shardIt.nextOrNull());
		}

		/**
		 * Perform first phase.
		 *
		 * @param shardIt the shard it
		 * @param shard the shard
		 */
		void performFirstPhase(final ShardIterator shardIt, final ShardRouting shard) {
			if (shard == null) {

				onFirstPhaseResult(null, shardIt, null);
			} else {
				DiscoveryNode node = nodes.get(shard.currentNodeId());
				if (node == null) {
					onFirstPhaseResult(shard, shardIt, null);
				} else {
					String[] filteringAliases = clusterState.metaData().filteringAliases(shard.index(),
							request.indices());
					sendExecuteFirstPhase(node,
							internalSearchRequest(shard, shardsIts.size(), request, filteringAliases, startTime),
							new SearchServiceListener<FirstResult>() {
								@Override
								public void onResult(FirstResult result) {
									onFirstPhaseResult(shard, result, shardIt);
								}

								@Override
								public void onFailure(Throwable t) {
									onFirstPhaseResult(shard, shardIt, t);
								}
							});
				}
			}
		}

		/**
		 * On first phase result.
		 *
		 * @param shard the shard
		 * @param result the result
		 * @param shardIt the shard it
		 */
		void onFirstPhaseResult(ShardRouting shard, FirstResult result, ShardIterator shardIt) {
			result.shardTarget(new SearchShardTarget(shard.currentNodeId(), shard.index(), shard.id()));
			processFirstPhaseResult(shard, result);

			int xTotalOps = totalOps.addAndGet(shardIt.remaining() + 1);
			successulOps.incrementAndGet();
			if (xTotalOps == expectedTotalOps) {
				try {
					moveToSecondPhase();
				} catch (Exception e) {
					if (logger.isDebugEnabled()) {
						logger.debug(shardIt.shardId() + ": Failed to execute [" + request
								+ "] while moving to second phase", e);
					}
					listener.onFailure(new ReduceSearchPhaseException(firstPhaseName(), "", e, buildShardFailures()));
				}
			}
		}

		/**
		 * On first phase result.
		 *
		 * @param shard the shard
		 * @param shardIt the shard it
		 * @param t the t
		 */
		void onFirstPhaseResult(@Nullable ShardRouting shard, final ShardIterator shardIt, Throwable t) {
			if (totalOps.incrementAndGet() == expectedTotalOps) {

				if (logger.isDebugEnabled()) {
					if (t != null) {
						if (shard != null) {
							logger.debug(shard.shortSummary() + ": Failed to execute [" + request + "]", t);
						} else {
							logger.debug(shardIt.shardId() + ": Failed to execute [" + request + "]", t);
						}
					}
				}

				if (t == null) {

					addShardFailure(new ShardSearchFailure("No active shards", new SearchShardTarget(null, shardIt
							.shardId().index().name(), shardIt.shardId().id())));
				} else {
					addShardFailure(new ShardSearchFailure(t));
				}
				if (successulOps.get() == 0) {

					listener.onFailure(new SearchPhaseExecutionException(firstPhaseName(), "total failure",
							buildShardFailures()));
				} else {
					try {
						moveToSecondPhase();
					} catch (Exception e) {
						listener.onFailure(new ReduceSearchPhaseException(firstPhaseName(), "", e, buildShardFailures()));
					}
				}
			} else {
				ShardRouting nextShard = shardIt.nextOrNull();
				if (nextShard != null) {

					if (logger.isTraceEnabled()) {
						if (t != null) {
							if (shard != null) {
								logger.trace(shard.shortSummary() + ": Failed to execute [" + request + "]", t);
							} else {
								logger.trace(shardIt.shardId() + ": Failed to execute [" + request + "]", t);
							}
						}
					}
					performFirstPhase(shardIt, nextShard);
				} else {

					if (logger.isDebugEnabled()) {
						if (t != null) {
							if (shard != null) {
								logger.debug(shard.shortSummary() + ": Failed to execute [" + request + "]", t);
							} else {
								logger.debug(shardIt.shardId() + ": Failed to execute [" + request + "]", t);
							}
						}
					}
					if (t == null) {

						addShardFailure(new ShardSearchFailure("No active shards", new SearchShardTarget(null, shardIt
								.shardId().index().name(), shardIt.shardId().id())));
					} else {
						addShardFailure(new ShardSearchFailure(t));
					}
				}
			}
		}

		/**
		 * Builds the took in millis.
		 *
		 * @return the long
		 */
		protected final long buildTookInMillis() {
			return System.currentTimeMillis() - startTime;
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
		 * Release irrelevant search contexts.
		 *
		 * @param queryResults the query results
		 * @param docIdsToLoad the doc ids to load
		 */
		protected void releaseIrrelevantSearchContexts(Map<SearchShardTarget, QuerySearchResultProvider> queryResults,
				Map<SearchShardTarget, ExtTIntArrayList> docIdsToLoad) {
			if (docIdsToLoad == null) {
				return;
			}

			if (request.scroll() == null) {
				for (Map.Entry<SearchShardTarget, QuerySearchResultProvider> entry : queryResults.entrySet()) {
					if (!docIdsToLoad.containsKey(entry.getKey())) {
						DiscoveryNode node = nodes.get(entry.getKey().nodeId());
						if (node != null) {
							searchService.sendFreeContext(node, entry.getValue().id());
						}
					}
				}
			}
		}

		/**
		 * Send execute first phase.
		 *
		 * @param node the node
		 * @param request the request
		 * @param listener the listener
		 */
		protected abstract void sendExecuteFirstPhase(DiscoveryNode node, InternalSearchRequest request,
				SearchServiceListener<FirstResult> listener);

		/**
		 * Process first phase result.
		 *
		 * @param shard the shard
		 * @param result the result
		 */
		protected abstract void processFirstPhaseResult(ShardRouting shard, FirstResult result);

		/**
		 * Move to second phase.
		 *
		 * @throws Exception the exception
		 */
		protected abstract void moveToSecondPhase() throws Exception;

		/**
		 * First phase name.
		 *
		 * @return the string
		 */
		protected abstract String firstPhaseName();
	}
}
