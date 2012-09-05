/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportSearchScrollAction.java 2012-7-6 14:29:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search;

import static cn.com.rebirth.search.core.action.search.type.ParsedScrollId.QUERY_AND_FETCH_TYPE;
import static cn.com.rebirth.search.core.action.search.type.ParsedScrollId.QUERY_THEN_FETCH_TYPE;
import static cn.com.rebirth.search.core.action.search.type.ParsedScrollId.SCAN;
import static cn.com.rebirth.search.core.action.search.type.TransportSearchHelper.parseScrollId;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.search.type.ParsedScrollId;
import cn.com.rebirth.search.core.action.search.type.TransportSearchScrollQueryAndFetchAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchScrollQueryThenFetchAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchScrollScanAction;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportSearchScrollAction.
 *
 * @author l.xue.nong
 */
public class TransportSearchScrollAction extends TransportAction<SearchScrollRequest, SearchResponse> {

	/** The query then fetch action. */
	private final TransportSearchScrollQueryThenFetchAction queryThenFetchAction;

	/** The query and fetch action. */
	private final TransportSearchScrollQueryAndFetchAction queryAndFetchAction;

	/** The scan action. */
	private final TransportSearchScrollScanAction scanAction;

	/**
	 * Instantiates a new transport search scroll action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param queryThenFetchAction the query then fetch action
	 * @param queryAndFetchAction the query and fetch action
	 * @param scanAction the scan action
	 */
	@Inject
	public TransportSearchScrollAction(Settings settings, ThreadPool threadPool, TransportService transportService,
			TransportSearchScrollQueryThenFetchAction queryThenFetchAction,
			TransportSearchScrollQueryAndFetchAction queryAndFetchAction, TransportSearchScrollScanAction scanAction) {
		super(settings, threadPool);
		this.queryThenFetchAction = queryThenFetchAction;
		this.queryAndFetchAction = queryAndFetchAction;
		this.scanAction = scanAction;

		transportService.registerHandler(SearchScrollAction.NAME, new TransportHandler());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.TransportAction#doExecute(cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(SearchScrollRequest request, ActionListener<SearchResponse> listener) {
		try {
			ParsedScrollId scrollId = parseScrollId(request.scrollId());
			if (scrollId.type().equals(QUERY_THEN_FETCH_TYPE)) {
				queryThenFetchAction.execute(request, scrollId, listener);
			} else if (scrollId.type().equals(QUERY_AND_FETCH_TYPE)) {
				queryAndFetchAction.execute(request, scrollId, listener);
			} else if (scrollId.type().equals(SCAN)) {
				scanAction.execute(request, scrollId, listener);
			} else {
				throw new RebirthIllegalArgumentException("Scroll id type [" + scrollId.type() + "] unrecognized");
			}
		} catch (Exception e) {
			listener.onFailure(e);
		}
	}

	/**
	 * The Class TransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class TransportHandler extends BaseTransportRequestHandler<SearchScrollRequest> {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public SearchScrollRequest newInstance() {
			return new SearchScrollRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(SearchScrollRequest request, final TransportChannel channel) throws Exception {
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