/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchServiceTransportAction.java 2012-3-29 15:02:48 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.stream.LongStreamable;
import cn.com.rebirth.search.commons.io.stream.VoidStreamable;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.search.SearchService;
import cn.com.rebirth.search.core.search.dfs.DfsSearchResult;
import cn.com.rebirth.search.core.search.fetch.FetchSearchRequest;
import cn.com.rebirth.search.core.search.fetch.FetchSearchResult;
import cn.com.rebirth.search.core.search.fetch.QueryFetchSearchResult;
import cn.com.rebirth.search.core.search.fetch.ScrollQueryFetchSearchResult;
import cn.com.rebirth.search.core.search.internal.InternalScrollSearchRequest;
import cn.com.rebirth.search.core.search.internal.InternalSearchRequest;
import cn.com.rebirth.search.core.search.query.QuerySearchRequest;
import cn.com.rebirth.search.core.search.query.QuerySearchResult;
import cn.com.rebirth.search.core.search.query.ScrollQuerySearchResult;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.BaseTransportResponseHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportService;
import cn.com.rebirth.search.core.transport.VoidTransportResponseHandler;


/**
 * The Class SearchServiceTransportAction.
 *
 * @author l.xue.nong
 */
public class SearchServiceTransportAction extends AbstractComponent {

	
	/**
	 * The Class FreeContextResponseHandler.
	 *
	 * @author l.xue.nong
	 */
	static final class FreeContextResponseHandler extends VoidTransportResponseHandler {

		
		/** The logger. */
		private final Logger logger = LoggerFactory.getLogger(getClass());

		
		/**
		 * Instantiates a new free context response handler.
		 */
		FreeContextResponseHandler() {
			super(ThreadPool.Names.SAME);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.VoidTransportResponseHandler#handleException(cn.com.summall.search.core.transport.TransportException)
		 */
		@Override
		public void handleException(TransportException exp) {
			logger.warn("Failed to send release search context", exp);
		}
	}

	
	/** The transport service. */
	private final TransportService transportService;

	
	/** The cluster service. */
	private final ClusterService clusterService;

	
	/** The search service. */
	private final SearchService searchService;

	
	/** The free context response handler. */
	private final FreeContextResponseHandler freeContextResponseHandler = new FreeContextResponseHandler();

	
	/**
	 * Instantiates a new search service transport action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param searchService the search service
	 */
	@Inject
	public SearchServiceTransportAction(Settings settings, TransportService transportService,
			ClusterService clusterService, SearchService searchService) {
		super(settings);
		this.transportService = transportService;
		this.clusterService = clusterService;
		this.searchService = searchService;

		transportService.registerHandler(SearchFreeContextTransportHandler.ACTION,
				new SearchFreeContextTransportHandler());
		transportService.registerHandler(SearchDfsTransportHandler.ACTION, new SearchDfsTransportHandler());
		transportService.registerHandler(SearchQueryTransportHandler.ACTION, new SearchQueryTransportHandler());
		transportService.registerHandler(SearchQueryByIdTransportHandler.ACTION, new SearchQueryByIdTransportHandler());
		transportService.registerHandler(SearchQueryScrollTransportHandler.ACTION,
				new SearchQueryScrollTransportHandler());
		transportService.registerHandler(SearchQueryFetchTransportHandler.ACTION,
				new SearchQueryFetchTransportHandler());
		transportService.registerHandler(SearchQueryQueryFetchTransportHandler.ACTION,
				new SearchQueryQueryFetchTransportHandler());
		transportService.registerHandler(SearchQueryFetchScrollTransportHandler.ACTION,
				new SearchQueryFetchScrollTransportHandler());
		transportService.registerHandler(SearchFetchByIdTransportHandler.ACTION, new SearchFetchByIdTransportHandler());
		transportService.registerHandler(SearchScanTransportHandler.ACTION, new SearchScanTransportHandler());
		transportService.registerHandler(SearchScanScrollTransportHandler.ACTION,
				new SearchScanScrollTransportHandler());
	}

	
	/**
	 * Send free context.
	 *
	 * @param node the node
	 * @param contextId the context id
	 */
	public void sendFreeContext(DiscoveryNode node, final long contextId) {
		if (clusterService.state().nodes().localNodeId().equals(node.id())) {
			searchService.freeContext(contextId);
		} else {
			transportService.sendRequest(node, SearchFreeContextTransportHandler.ACTION, new LongStreamable(contextId),
					freeContextResponseHandler);
		}
	}

	
	/**
	 * Send execute dfs.
	 *
	 * @param node the node
	 * @param request the request
	 * @param listener the listener
	 */
	public void sendExecuteDfs(DiscoveryNode node, final InternalSearchRequest request,
			final SearchServiceListener<DfsSearchResult> listener) {
		if (clusterService.state().nodes().localNodeId().equals(node.id())) {
			try {
				DfsSearchResult result = searchService.executeDfsPhase(request);
				listener.onResult(result);
			} catch (Exception e) {
				listener.onFailure(e);
			}
		} else {
			transportService.sendRequest(node, SearchDfsTransportHandler.ACTION, request,
					new BaseTransportResponseHandler<DfsSearchResult>() {

						@Override
						public DfsSearchResult newInstance() {
							return new DfsSearchResult();
						}

						@Override
						public void handleResponse(DfsSearchResult response) {
							listener.onResult(response);
						}

						@Override
						public void handleException(TransportException exp) {
							listener.onFailure(exp);
						}

						@Override
						public String executor() {
							return ThreadPool.Names.SAME;
						}
					});
		}
	}

	
	/**
	 * Send execute query.
	 *
	 * @param node the node
	 * @param request the request
	 * @param listener the listener
	 */
	public void sendExecuteQuery(DiscoveryNode node, final InternalSearchRequest request,
			final SearchServiceListener<QuerySearchResult> listener) {
		if (clusterService.state().nodes().localNodeId().equals(node.id())) {
			try {
				QuerySearchResult result = searchService.executeQueryPhase(request);
				listener.onResult(result);
			} catch (Exception e) {
				listener.onFailure(e);
			}
		} else {
			transportService.sendRequest(node, SearchQueryTransportHandler.ACTION, request,
					new BaseTransportResponseHandler<QuerySearchResult>() {

						@Override
						public QuerySearchResult newInstance() {
							return new QuerySearchResult();
						}

						@Override
						public void handleResponse(QuerySearchResult response) {
							listener.onResult(response);
						}

						@Override
						public void handleException(TransportException exp) {
							listener.onFailure(exp);
						}

						@Override
						public String executor() {
							return ThreadPool.Names.SAME;
						}
					});
		}
	}

	
	/**
	 * Send execute query.
	 *
	 * @param node the node
	 * @param request the request
	 * @param listener the listener
	 */
	public void sendExecuteQuery(DiscoveryNode node, final QuerySearchRequest request,
			final SearchServiceListener<QuerySearchResult> listener) {
		if (clusterService.state().nodes().localNodeId().equals(node.id())) {
			try {
				QuerySearchResult result = searchService.executeQueryPhase(request);
				listener.onResult(result);
			} catch (Exception e) {
				listener.onFailure(e);
			}
		} else {
			transportService.sendRequest(node, SearchQueryByIdTransportHandler.ACTION, request,
					new BaseTransportResponseHandler<QuerySearchResult>() {

						@Override
						public QuerySearchResult newInstance() {
							return new QuerySearchResult();
						}

						@Override
						public void handleResponse(QuerySearchResult response) {
							listener.onResult(response);
						}

						@Override
						public void handleException(TransportException exp) {
							listener.onFailure(exp);
						}

						@Override
						public String executor() {
							return ThreadPool.Names.SAME;
						}
					});
		}
	}

	
	/**
	 * Send execute query.
	 *
	 * @param node the node
	 * @param request the request
	 * @param listener the listener
	 */
	public void sendExecuteQuery(DiscoveryNode node, final InternalScrollSearchRequest request,
			final SearchServiceListener<QuerySearchResult> listener) {
		if (clusterService.state().nodes().localNodeId().equals(node.id())) {
			try {
				ScrollQuerySearchResult result = searchService.executeQueryPhase(request);
				listener.onResult(result.queryResult());
			} catch (Exception e) {
				listener.onFailure(e);
			}
		} else {
			transportService.sendRequest(node, SearchQueryScrollTransportHandler.ACTION, request,
					new BaseTransportResponseHandler<ScrollQuerySearchResult>() {

						@Override
						public ScrollQuerySearchResult newInstance() {
							return new ScrollQuerySearchResult();
						}

						@Override
						public void handleResponse(ScrollQuerySearchResult response) {
							listener.onResult(response.queryResult());
						}

						@Override
						public void handleException(TransportException exp) {
							listener.onFailure(exp);
						}

						@Override
						public String executor() {
							return ThreadPool.Names.SAME;
						}
					});
		}
	}

	
	/**
	 * Send execute fetch.
	 *
	 * @param node the node
	 * @param request the request
	 * @param listener the listener
	 */
	public void sendExecuteFetch(DiscoveryNode node, final InternalSearchRequest request,
			final SearchServiceListener<QueryFetchSearchResult> listener) {
		if (clusterService.state().nodes().localNodeId().equals(node.id())) {
			try {
				QueryFetchSearchResult result = searchService.executeFetchPhase(request);
				listener.onResult(result);
			} catch (Exception e) {
				listener.onFailure(e);
			}
		} else {
			transportService.sendRequest(node, SearchQueryFetchTransportHandler.ACTION, request,
					new BaseTransportResponseHandler<QueryFetchSearchResult>() {

						@Override
						public QueryFetchSearchResult newInstance() {
							return new QueryFetchSearchResult();
						}

						@Override
						public void handleResponse(QueryFetchSearchResult response) {
							listener.onResult(response);
						}

						@Override
						public void handleException(TransportException exp) {
							listener.onFailure(exp);
						}

						@Override
						public String executor() {
							return ThreadPool.Names.SAME;
						}
					});
		}
	}

	
	/**
	 * Send execute fetch.
	 *
	 * @param node the node
	 * @param request the request
	 * @param listener the listener
	 */
	public void sendExecuteFetch(DiscoveryNode node, final QuerySearchRequest request,
			final SearchServiceListener<QueryFetchSearchResult> listener) {
		if (clusterService.state().nodes().localNodeId().equals(node.id())) {
			try {
				QueryFetchSearchResult result = searchService.executeFetchPhase(request);
				listener.onResult(result);
			} catch (Exception e) {
				listener.onFailure(e);
			}
		} else {
			transportService.sendRequest(node, SearchQueryQueryFetchTransportHandler.ACTION, request,
					new BaseTransportResponseHandler<QueryFetchSearchResult>() {

						@Override
						public QueryFetchSearchResult newInstance() {
							return new QueryFetchSearchResult();
						}

						@Override
						public void handleResponse(QueryFetchSearchResult response) {
							listener.onResult(response);
						}

						@Override
						public void handleException(TransportException exp) {
							listener.onFailure(exp);
						}

						@Override
						public String executor() {
							return ThreadPool.Names.SAME;
						}
					});
		}
	}

	
	/**
	 * Send execute fetch.
	 *
	 * @param node the node
	 * @param request the request
	 * @param listener the listener
	 */
	public void sendExecuteFetch(DiscoveryNode node, final InternalScrollSearchRequest request,
			final SearchServiceListener<QueryFetchSearchResult> listener) {
		if (clusterService.state().nodes().localNodeId().equals(node.id())) {
			try {
				ScrollQueryFetchSearchResult result = searchService.executeFetchPhase(request);
				listener.onResult(result.result());
			} catch (Exception e) {
				listener.onFailure(e);
			}
		} else {
			transportService.sendRequest(node, SearchQueryFetchScrollTransportHandler.ACTION, request,
					new BaseTransportResponseHandler<ScrollQueryFetchSearchResult>() {

						@Override
						public ScrollQueryFetchSearchResult newInstance() {
							return new ScrollQueryFetchSearchResult();
						}

						@Override
						public void handleResponse(ScrollQueryFetchSearchResult response) {
							listener.onResult(response.result());
						}

						@Override
						public void handleException(TransportException exp) {
							listener.onFailure(exp);
						}

						@Override
						public String executor() {
							return ThreadPool.Names.SAME;
						}
					});
		}
	}

	
	/**
	 * Send execute fetch.
	 *
	 * @param node the node
	 * @param request the request
	 * @param listener the listener
	 */
	public void sendExecuteFetch(DiscoveryNode node, final FetchSearchRequest request,
			final SearchServiceListener<FetchSearchResult> listener) {
		if (clusterService.state().nodes().localNodeId().equals(node.id())) {
			try {
				FetchSearchResult result = searchService.executeFetchPhase(request);
				listener.onResult(result);
			} catch (Exception e) {
				listener.onFailure(e);
			}
		} else {
			transportService.sendRequest(node, SearchFetchByIdTransportHandler.ACTION, request,
					new BaseTransportResponseHandler<FetchSearchResult>() {

						@Override
						public FetchSearchResult newInstance() {
							return new FetchSearchResult();
						}

						@Override
						public void handleResponse(FetchSearchResult response) {
							listener.onResult(response);
						}

						@Override
						public void handleException(TransportException exp) {
							listener.onFailure(exp);
						}

						@Override
						public String executor() {
							return ThreadPool.Names.SAME;
						}
					});
		}
	}

	
	/**
	 * Send execute scan.
	 *
	 * @param node the node
	 * @param request the request
	 * @param listener the listener
	 */
	public void sendExecuteScan(DiscoveryNode node, final InternalSearchRequest request,
			final SearchServiceListener<QuerySearchResult> listener) {
		if (clusterService.state().nodes().localNodeId().equals(node.id())) {
			try {
				QuerySearchResult result = searchService.executeScan(request);
				listener.onResult(result);
			} catch (Exception e) {
				listener.onFailure(e);
			}
		} else {
			transportService.sendRequest(node, SearchScanTransportHandler.ACTION, request,
					new BaseTransportResponseHandler<QuerySearchResult>() {

						@Override
						public QuerySearchResult newInstance() {
							return new QuerySearchResult();
						}

						@Override
						public void handleResponse(QuerySearchResult response) {
							listener.onResult(response);
						}

						@Override
						public void handleException(TransportException exp) {
							listener.onFailure(exp);
						}

						@Override
						public String executor() {
							return ThreadPool.Names.SAME;
						}
					});
		}
	}

	
	/**
	 * Send execute scan.
	 *
	 * @param node the node
	 * @param request the request
	 * @param listener the listener
	 */
	public void sendExecuteScan(DiscoveryNode node, final InternalScrollSearchRequest request,
			final SearchServiceListener<QueryFetchSearchResult> listener) {
		if (clusterService.state().nodes().localNodeId().equals(node.id())) {
			try {
				ScrollQueryFetchSearchResult result = searchService.executeScan(request);
				listener.onResult(result.result());
			} catch (Exception e) {
				listener.onFailure(e);
			}
		} else {
			transportService.sendRequest(node, SearchScanScrollTransportHandler.ACTION, request,
					new BaseTransportResponseHandler<ScrollQueryFetchSearchResult>() {

						@Override
						public ScrollQueryFetchSearchResult newInstance() {
							return new ScrollQueryFetchSearchResult();
						}

						@Override
						public void handleResponse(ScrollQueryFetchSearchResult response) {
							listener.onResult(response.result());
						}

						@Override
						public void handleException(TransportException exp) {
							listener.onFailure(exp);
						}

						@Override
						public String executor() {
							return ThreadPool.Names.SAME;
						}
					});
		}
	}

	
	/**
	 * The Class SearchFreeContextTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class SearchFreeContextTransportHandler extends BaseTransportRequestHandler<LongStreamable> {

		
		/** The Constant ACTION. */
		static final String ACTION = "search/freeContext";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public LongStreamable newInstance() {
			return new LongStreamable();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(LongStreamable request, TransportChannel channel) throws Exception {
			searchService.freeContext(request.get());
			channel.sendResponse(VoidStreamable.INSTANCE);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SEARCH;
		}
	}

	
	/**
	 * The Class SearchDfsTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class SearchDfsTransportHandler extends BaseTransportRequestHandler<InternalSearchRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "search/phase/dfs";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public InternalSearchRequest newInstance() {
			return new InternalSearchRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(InternalSearchRequest request, TransportChannel channel) throws Exception {
			DfsSearchResult result = searchService.executeDfsPhase(request);
			channel.sendResponse(result);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SEARCH;
		}
	}

	
	/**
	 * The Class SearchQueryTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class SearchQueryTransportHandler extends BaseTransportRequestHandler<InternalSearchRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "search/phase/query";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public InternalSearchRequest newInstance() {
			return new InternalSearchRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(InternalSearchRequest request, TransportChannel channel) throws Exception {
			QuerySearchResult result = searchService.executeQueryPhase(request);
			channel.sendResponse(result);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SEARCH;
		}
	}

	
	/**
	 * The Class SearchQueryByIdTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class SearchQueryByIdTransportHandler extends BaseTransportRequestHandler<QuerySearchRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "search/phase/query/id";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public QuerySearchRequest newInstance() {
			return new QuerySearchRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(QuerySearchRequest request, TransportChannel channel) throws Exception {
			QuerySearchResult result = searchService.executeQueryPhase(request);
			channel.sendResponse(result);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SEARCH;
		}
	}

	
	/**
	 * The Class SearchQueryScrollTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class SearchQueryScrollTransportHandler extends BaseTransportRequestHandler<InternalScrollSearchRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "search/phase/query/scroll";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public InternalScrollSearchRequest newInstance() {
			return new InternalScrollSearchRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(InternalScrollSearchRequest request, TransportChannel channel) throws Exception {
			ScrollQuerySearchResult result = searchService.executeQueryPhase(request);
			channel.sendResponse(result);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SEARCH;
		}
	}

	
	/**
	 * The Class SearchQueryFetchTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class SearchQueryFetchTransportHandler extends BaseTransportRequestHandler<InternalSearchRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "search/phase/query+fetch";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public InternalSearchRequest newInstance() {
			return new InternalSearchRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(InternalSearchRequest request, TransportChannel channel) throws Exception {
			QueryFetchSearchResult result = searchService.executeFetchPhase(request);
			channel.sendResponse(result);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SEARCH;
		}
	}

	
	/**
	 * The Class SearchQueryQueryFetchTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class SearchQueryQueryFetchTransportHandler extends BaseTransportRequestHandler<QuerySearchRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "search/phase/query/query+fetch";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public QuerySearchRequest newInstance() {
			return new QuerySearchRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(QuerySearchRequest request, TransportChannel channel) throws Exception {
			QueryFetchSearchResult result = searchService.executeFetchPhase(request);
			channel.sendResponse(result);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SEARCH;
		}
	}

	
	/**
	 * The Class SearchFetchByIdTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class SearchFetchByIdTransportHandler extends BaseTransportRequestHandler<FetchSearchRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "search/phase/fetch/id";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public FetchSearchRequest newInstance() {
			return new FetchSearchRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(FetchSearchRequest request, TransportChannel channel) throws Exception {
			FetchSearchResult result = searchService.executeFetchPhase(request);
			channel.sendResponse(result);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SEARCH;
		}
	}

	
	/**
	 * The Class SearchQueryFetchScrollTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class SearchQueryFetchScrollTransportHandler extends
			BaseTransportRequestHandler<InternalScrollSearchRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "search/phase/query+fetch/scroll";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public InternalScrollSearchRequest newInstance() {
			return new InternalScrollSearchRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(InternalScrollSearchRequest request, TransportChannel channel) throws Exception {
			ScrollQueryFetchSearchResult result = searchService.executeFetchPhase(request);
			channel.sendResponse(result);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SEARCH;
		}
	}

	
	/**
	 * The Class SearchScanTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class SearchScanTransportHandler extends BaseTransportRequestHandler<InternalSearchRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "search/phase/scan";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public InternalSearchRequest newInstance() {
			return new InternalSearchRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(InternalSearchRequest request, TransportChannel channel) throws Exception {
			QuerySearchResult result = searchService.executeScan(request);
			channel.sendResponse(result);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SEARCH;
		}
	}

	
	/**
	 * The Class SearchScanScrollTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class SearchScanScrollTransportHandler extends BaseTransportRequestHandler<InternalScrollSearchRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "search/phase/scan/scroll";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public InternalScrollSearchRequest newInstance() {
			return new InternalScrollSearchRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(InternalScrollSearchRequest request, TransportChannel channel) throws Exception {
			ScrollQueryFetchSearchResult result = searchService.executeScan(request);
			channel.sendResponse(result);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SEARCH;
		}
	}
}
