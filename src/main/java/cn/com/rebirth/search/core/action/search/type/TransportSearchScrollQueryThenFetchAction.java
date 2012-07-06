/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportSearchScrollQueryThenFetchAction.java 2012-3-29 15:02:30 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.search.type;

import static cn.com.rebirth.search.core.action.search.type.TransportSearchHelper.internalScrollSearchRequest;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jsr166y.LinkedTransferQueue;
import cn.com.rebirth.commons.collect.Tuple;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.trove.ExtTIntArrayList;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
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
import cn.com.rebirth.search.core.search.fetch.FetchSearchRequest;
import cn.com.rebirth.search.core.search.fetch.FetchSearchResult;
import cn.com.rebirth.search.core.search.internal.InternalSearchResponse;
import cn.com.rebirth.search.core.search.query.QuerySearchResult;
import cn.com.rebirth.search.core.search.query.QuerySearchResultProvider;
import cn.com.rebirth.search.core.threadpool.ThreadPool;


/**
 * The Class TransportSearchScrollQueryThenFetchAction.
 *
 * @author l.xue.nong
 */
public class TransportSearchScrollQueryThenFetchAction extends AbstractComponent {

    
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
     * Instantiates a new transport search scroll query then fetch action.
     *
     * @param settings the settings
     * @param threadPool the thread pool
     * @param clusterService the cluster service
     * @param searchCache the search cache
     * @param searchService the search service
     * @param searchPhaseController the search phase controller
     */
    @Inject
    public TransportSearchScrollQueryThenFetchAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
                                                     TransportSearchCache searchCache,
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
        protected volatile LinkedTransferQueue<ShardSearchFailure> shardFailures;

        
        /** The query results. */
        private final Map<SearchShardTarget, QuerySearchResultProvider> queryResults = searchCache.obtainQueryResults();

        
        /** The fetch results. */
        private final Map<SearchShardTarget, FetchSearchResult> fetchResults = searchCache.obtainFetchResults();

        
        /** The sorted shard list. */
        private volatile ShardDoc[] sortedShardList;

        
        /** The successful ops. */
        private final AtomicInteger successfulOps;

        
        /** The start time. */
        private final long startTime = System.currentTimeMillis();

        
        /**
         * Instantiates a new async action.
         *
         * @param request the request
         * @param scrollId the scroll id
         * @param listener the listener
         */
        private AsyncAction(SearchScrollRequest request, ParsedScrollId scrollId, ActionListener<SearchResponse> listener) {
            this.request = request;
            this.listener = listener;
            this.scrollId = scrollId;
            this.nodes = clusterService.state().nodes();
            this.successfulOps = new AtomicInteger(scrollId.context().length);
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
            final AtomicInteger counter = new AtomicInteger(scrollId.context().length);

            int localOperations = 0;
            for (Tuple<String, Long> target : scrollId.context()) {
                DiscoveryNode node = nodes.get(target.v1());
                if (node != null) {
                    if (nodes.localNodeId().equals(node.id())) {
                        localOperations++;
                    } else {
                        executeQueryPhase(counter, node, target.v2());
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Node [" + target.v1() + "] not available for scroll request [" + scrollId.source() + "]");
                    }
                    successfulOps.decrementAndGet();
                    if (counter.decrementAndGet() == 0) {
                        executeFetchPhase();
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
                                    executeQueryPhase(counter, node, target.v2());
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
                                        executeQueryPhase(counter, node, target.v2());
                                    }
                                });
                            } else {
                                executeQueryPhase(counter, node, target.v2());
                            }
                        }
                    }
                }
            }
        }

        
        /**
         * Execute query phase.
         *
         * @param counter the counter
         * @param node the node
         * @param searchId the search id
         */
        private void executeQueryPhase(final AtomicInteger counter, DiscoveryNode node, final long searchId) {
            searchService.sendExecuteQuery(node, internalScrollSearchRequest(searchId, request), new SearchServiceListener<QuerySearchResult>() {
                @Override
                public void onResult(QuerySearchResult result) {
                    queryResults.put(result.shardTarget(), result);
                    if (counter.decrementAndGet() == 0) {
                        executeFetchPhase();
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
                        executeFetchPhase();
                    }
                }
            });
        }

        
        /**
         * Execute fetch phase.
         */
        private void executeFetchPhase() {
            sortedShardList = searchPhaseController.sortDocs(queryResults.values());
            Map<SearchShardTarget, ExtTIntArrayList> docIdsToLoad = searchPhaseController.docIdsToLoad(sortedShardList);

            if (docIdsToLoad.isEmpty()) {
                finishHim();
            }

            final AtomicInteger counter = new AtomicInteger(docIdsToLoad.size());

            for (final Map.Entry<SearchShardTarget, ExtTIntArrayList> entry : docIdsToLoad.entrySet()) {
                SearchShardTarget shardTarget = entry.getKey();
                ExtTIntArrayList docIds = entry.getValue();
                FetchSearchRequest fetchSearchRequest = new FetchSearchRequest(queryResults.get(shardTarget).id(), docIds);
                DiscoveryNode node = nodes.get(shardTarget.nodeId());
                searchService.sendExecuteFetch(node, fetchSearchRequest, new SearchServiceListener<FetchSearchResult>() {
                    @Override
                    public void onResult(FetchSearchResult result) {
                        result.shardTarget(entry.getKey());
                        fetchResults.put(result.shardTarget(), result);
                        if (counter.decrementAndGet() == 0) {
                            finishHim();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Failed to execute fetch phase", t);
                        }
                        successfulOps.decrementAndGet();
                        if (counter.decrementAndGet() == 0) {
                            finishHim();
                        }
                    }
                });
            }
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
            InternalSearchResponse internalResponse = searchPhaseController.merge(sortedShardList, queryResults, fetchResults);
            String scrollId = null;
            if (request.scroll() != null) {
                scrollId = request.scrollId();
            }
            listener.onResponse(new SearchResponse(internalResponse, scrollId, this.scrollId.context().length, successfulOps.get(),
                    System.currentTimeMillis() - startTime, buildShardFailures()));
            searchCache.releaseQueryResults(queryResults);
            searchCache.releaseFetchResults(fetchResults);
        }
    }
}