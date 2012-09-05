/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportMultiSearchAction.java 2012-7-6 14:28:53 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search;

import java.util.concurrent.atomic.AtomicInteger;

import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportMultiSearchAction.
 *
 * @author l.xue.nong
 */
public class TransportMultiSearchAction extends TransportAction<MultiSearchRequest, MultiSearchResponse> {

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The search action. */
	private final TransportSearchAction searchAction;

	/**
	 * Instantiates a new transport multi search action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param searchAction the search action
	 */
	@Inject
	public TransportMultiSearchAction(Settings settings, ThreadPool threadPool, TransportService transportService,
			ClusterService clusterService, TransportSearchAction searchAction) {
		super(settings, threadPool);
		this.clusterService = clusterService;
		this.searchAction = searchAction;

		transportService.registerHandler(MultiSearchAction.NAME, new TransportHandler());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.TransportAction#doExecute(cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(final MultiSearchRequest request, final ActionListener<MultiSearchResponse> listener) {
		ClusterState clusterState = clusterService.state();
		clusterState.blocks().globalBlockedRaiseException(ClusterBlockLevel.READ);

		final MultiSearchResponse.Item[] responses = new MultiSearchResponse.Item[request.requests().size()];
		final AtomicInteger counter = new AtomicInteger(responses.length);
		for (int i = 0; i < responses.length; i++) {
			final int index = i;
			searchAction.execute(request.requests().get(i), new ActionListener<SearchResponse>() {
				@Override
				public void onResponse(SearchResponse searchResponse) {
					synchronized (responses) {
						responses[index] = new MultiSearchResponse.Item(searchResponse, null);
					}
					if (counter.decrementAndGet() == 0) {
						finishHim();
					}
				}

				@Override
				public void onFailure(Throwable e) {
					synchronized (responses) {
						responses[index] = new MultiSearchResponse.Item(null, ExceptionsHelper.detailedMessage(e));
					}
					if (counter.decrementAndGet() == 0) {
						finishHim();
					}
				}

				private void finishHim() {
					listener.onResponse(new MultiSearchResponse(responses));
				}
			});
		}
	}

	/**
	 * The Class TransportHandler.
	 *
	 * @author l.xue.nong
	 */
	class TransportHandler extends BaseTransportRequestHandler<MultiSearchRequest> {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public MultiSearchRequest newInstance() {
			return new MultiSearchRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(final MultiSearchRequest request, final TransportChannel channel) throws Exception {

			request.listenerThreaded(false);
			execute(request, new ActionListener<MultiSearchResponse>() {
				@Override
				public void onResponse(MultiSearchResponse response) {
					try {
						channel.sendResponse(response);
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(e);
					} catch (Exception e1) {
						logger.warn("Failed to send error response for action [msearch] and request [" + request + "]",
								e1);
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
