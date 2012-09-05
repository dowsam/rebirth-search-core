/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportSearchAction.java 2012-7-6 14:30:30 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search;

import static cn.com.rebirth.search.core.action.search.SearchType.COUNT;
import static cn.com.rebirth.search.core.action.search.SearchType.DFS_QUERY_THEN_FETCH;
import static cn.com.rebirth.search.core.action.search.SearchType.QUERY_AND_FETCH;
import static cn.com.rebirth.search.core.action.search.SearchType.SCAN;

import java.util.Map;
import java.util.Set;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.search.type.TransportSearchCountAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchDfsQueryAndFetchAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchDfsQueryThenFetchAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchQueryAndFetchAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchQueryThenFetchAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchScanAction;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.indices.IndexMissingException;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportSearchAction.
 *
 * @author l.xue.nong
 */
public class TransportSearchAction extends TransportAction<SearchRequest, SearchResponse> {

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The dfs query then fetch action. */
	private final TransportSearchDfsQueryThenFetchAction dfsQueryThenFetchAction;

	/** The query then fetch action. */
	private final TransportSearchQueryThenFetchAction queryThenFetchAction;

	/** The dfs query and fetch action. */
	private final TransportSearchDfsQueryAndFetchAction dfsQueryAndFetchAction;

	/** The query and fetch action. */
	private final TransportSearchQueryAndFetchAction queryAndFetchAction;

	/** The scan action. */
	private final TransportSearchScanAction scanAction;

	/** The count action. */
	private final TransportSearchCountAction countAction;

	/** The optimize single shard. */
	private final boolean optimizeSingleShard;

	/**
	 * Instantiates a new transport search action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param dfsQueryThenFetchAction the dfs query then fetch action
	 * @param queryThenFetchAction the query then fetch action
	 * @param dfsQueryAndFetchAction the dfs query and fetch action
	 * @param queryAndFetchAction the query and fetch action
	 * @param scanAction the scan action
	 * @param countAction the count action
	 */
	@Inject
	public TransportSearchAction(Settings settings, ThreadPool threadPool, TransportService transportService,
			ClusterService clusterService, TransportSearchDfsQueryThenFetchAction dfsQueryThenFetchAction,
			TransportSearchQueryThenFetchAction queryThenFetchAction,
			TransportSearchDfsQueryAndFetchAction dfsQueryAndFetchAction,
			TransportSearchQueryAndFetchAction queryAndFetchAction, TransportSearchScanAction scanAction,
			TransportSearchCountAction countAction) {
		super(settings, threadPool);
		this.clusterService = clusterService;
		this.dfsQueryThenFetchAction = dfsQueryThenFetchAction;
		this.queryThenFetchAction = queryThenFetchAction;
		this.dfsQueryAndFetchAction = dfsQueryAndFetchAction;
		this.queryAndFetchAction = queryAndFetchAction;
		this.scanAction = scanAction;
		this.countAction = countAction;

		this.optimizeSingleShard = componentSettings.getAsBoolean("optimize_single_shard", true);

		transportService.registerHandler(SearchAction.NAME, new TransportHandler());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.TransportAction#doExecute(cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(SearchRequest searchRequest, ActionListener<SearchResponse> listener) {

		if (optimizeSingleShard && searchRequest.searchType() != SCAN && searchRequest.searchType() != COUNT) {
			try {
				ClusterState clusterState = clusterService.state();
				String[] concreteIndices = clusterState.metaData()
						.concreteIndices(searchRequest.indices(), false, true);
				Map<String, Set<String>> routingMap = clusterState.metaData().resolveSearchRouting(
						searchRequest.routing(), searchRequest.indices());
				int shardCount = clusterService.operationRouting().searchShardsCount(clusterState,
						searchRequest.indices(), concreteIndices, searchRequest.queryHint(), routingMap,
						searchRequest.preference());
				if (shardCount == 1) {

					searchRequest.searchType(QUERY_AND_FETCH);
				}
			} catch (IndexMissingException e) {

			} catch (Exception e) {
				logger.debug("failed to optimize search type, continue as normal", e);
			}
		}

		if (searchRequest.searchType() == DFS_QUERY_THEN_FETCH) {
			dfsQueryThenFetchAction.execute(searchRequest, listener);
		} else if (searchRequest.searchType() == SearchType.QUERY_THEN_FETCH) {
			queryThenFetchAction.execute(searchRequest, listener);
		} else if (searchRequest.searchType() == SearchType.DFS_QUERY_AND_FETCH) {
			dfsQueryAndFetchAction.execute(searchRequest, listener);
		} else if (searchRequest.searchType() == SearchType.QUERY_AND_FETCH) {
			queryAndFetchAction.execute(searchRequest, listener);
		} else if (searchRequest.searchType() == SearchType.SCAN) {
			scanAction.execute(searchRequest, listener);
		} else if (searchRequest.searchType() == SearchType.COUNT) {
			countAction.execute(searchRequest, listener);
		}
	}

	/**
	 * The Class TransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class TransportHandler extends BaseTransportRequestHandler<SearchRequest> {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public SearchRequest newInstance() {
			return new SearchRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(SearchRequest request, final TransportChannel channel) throws Exception {

			request.listenerThreaded(false);

			if (request.operationThreading() == SearchOperationThreading.NO_THREADS) {
				request.operationThreading(SearchOperationThreading.SINGLE_THREAD);
			}
			execute(request, new ActionListener<SearchResponse>() {
				@Override
				public void onResponse(SearchResponse result) {
					try {
						channel.sendResponse(result);
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(e);
					} catch (Exception e1) {
						logger.warn("Failed to send response for search", e1);
					}
				}
			});
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SAME;
		}
	}
}
