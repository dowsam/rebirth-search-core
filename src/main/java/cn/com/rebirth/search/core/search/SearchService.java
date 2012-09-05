/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchService.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.TopDocs;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.concurrent.ConcurrentMapLong;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.search.SearchType;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.search.stats.StatsGroupsParseElement;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.indices.IndicesLifecycle;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.script.ScriptService;
import cn.com.rebirth.search.core.search.dfs.CachedDfSource;
import cn.com.rebirth.search.core.search.dfs.DfsPhase;
import cn.com.rebirth.search.core.search.dfs.DfsSearchResult;
import cn.com.rebirth.search.core.search.fetch.FetchPhase;
import cn.com.rebirth.search.core.search.fetch.FetchSearchRequest;
import cn.com.rebirth.search.core.search.fetch.FetchSearchResult;
import cn.com.rebirth.search.core.search.fetch.QueryFetchSearchResult;
import cn.com.rebirth.search.core.search.fetch.ScrollQueryFetchSearchResult;
import cn.com.rebirth.search.core.search.internal.InternalScrollSearchRequest;
import cn.com.rebirth.search.core.search.internal.InternalSearchRequest;
import cn.com.rebirth.search.core.search.internal.SearchContext;
import cn.com.rebirth.search.core.search.query.QueryPhase;
import cn.com.rebirth.search.core.search.query.QueryPhaseExecutionException;
import cn.com.rebirth.search.core.search.query.QuerySearchRequest;
import cn.com.rebirth.search.core.search.query.QuerySearchResult;
import cn.com.rebirth.search.core.search.query.ScrollQuerySearchResult;

import com.google.common.collect.ImmutableMap;

/**
 * The Class SearchService.
 *
 * @author l.xue.nong
 */
public class SearchService extends AbstractLifecycleComponent<SearchService> {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The indices service. */
	private final IndicesService indicesService;

	/** The script service. */
	private final ScriptService scriptService;

	/** The dfs phase. */
	private final DfsPhase dfsPhase;

	/** The query phase. */
	private final QueryPhase queryPhase;

	/** The fetch phase. */
	private final FetchPhase fetchPhase;

	/** The default keep alive. */
	private final long defaultKeepAlive;

	/** The keep alive reaper. */
	private final ScheduledFuture keepAliveReaper;

	/** The id generator. */
	private final AtomicLong idGenerator = new AtomicLong();

	/** The indices lifecycle listener. */
	private final CleanContextOnIndicesLifecycleListener indicesLifecycleListener = new CleanContextOnIndicesLifecycleListener();

	/** The active contexts. */
	private final ConcurrentMapLong<SearchContext> activeContexts = ConcurrentCollections.newConcurrentMapLong();

	/** The element parsers. */
	private final ImmutableMap<String, SearchParseElement> elementParsers;

	/**
	 * Instantiates a new search service.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 * @param indicesService the indices service
	 * @param indicesLifecycle the indices lifecycle
	 * @param threadPool the thread pool
	 * @param scriptService the script service
	 * @param dfsPhase the dfs phase
	 * @param queryPhase the query phase
	 * @param fetchPhase the fetch phase
	 */
	@Inject
	public SearchService(Settings settings, ClusterService clusterService, IndicesService indicesService,
			IndicesLifecycle indicesLifecycle, ThreadPool threadPool, ScriptService scriptService, DfsPhase dfsPhase,
			QueryPhase queryPhase, FetchPhase fetchPhase) {
		super(settings);
		this.threadPool = threadPool;
		this.clusterService = clusterService;
		this.indicesService = indicesService;
		this.scriptService = scriptService;
		this.dfsPhase = dfsPhase;
		this.queryPhase = queryPhase;
		this.fetchPhase = fetchPhase;

		TimeValue keepAliveInterval = componentSettings.getAsTime("keep_alive_interval", TimeValue.timeValueMinutes(1));

		this.defaultKeepAlive = componentSettings.getAsTime("default_keep_alive", TimeValue.timeValueMinutes(5))
				.millis();

		Map<String, SearchParseElement> elementParsers = new HashMap<String, SearchParseElement>();
		elementParsers.putAll(dfsPhase.parseElements());
		elementParsers.putAll(queryPhase.parseElements());
		elementParsers.putAll(fetchPhase.parseElements());
		elementParsers.put("stats", new StatsGroupsParseElement());
		this.elementParsers = ImmutableMap.copyOf(elementParsers);
		indicesLifecycle.addListener(indicesLifecycleListener);

		this.keepAliveReaper = threadPool.scheduleWithFixedDelay(new Reaper(), keepAliveInterval);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RebirthException {
		for (SearchContext context : activeContexts.values()) {
			freeContext(context);
		}
		activeContexts.clear();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
		keepAliveReaper.cancel(false);
		indicesService.indicesLifecycle().removeListener(indicesLifecycleListener);
	}

	/**
	 * Release contexts for index.
	 *
	 * @param index the index
	 */
	public void releaseContextsForIndex(Index index) {
		for (SearchContext context : activeContexts.values()) {
			if (context.shardTarget().index().equals(index.name())) {
				freeContext(context);
			}
		}
	}

	/**
	 * Release contexts for shard.
	 *
	 * @param shardId the shard id
	 */
	public void releaseContextsForShard(ShardId shardId) {
		for (SearchContext context : activeContexts.values()) {
			if (context.shardTarget().index().equals(shardId.index().name())
					&& context.shardTarget().shardId() == shardId.id()) {
				freeContext(context);
			}
		}
	}

	/**
	 * Execute dfs phase.
	 *
	 * @param request the request
	 * @return the dfs search result
	 * @throws RebirthException the rebirth exception
	 */
	public DfsSearchResult executeDfsPhase(InternalSearchRequest request) throws RebirthException {
		SearchContext context = createContext(request);
		activeContexts.put(context.id(), context);
		try {
			contextProcessing(context);
			dfsPhase.execute(context);
			contextProcessedSuccessfully(context);
			return context.dfsResult();
		} catch (RuntimeException e) {
			logger.trace("Dfs phase failed", e);
			freeContext(context);
			throw e;
		} finally {
			cleanContext(context);
		}
	}

	/**
	 * Execute scan.
	 *
	 * @param request the request
	 * @return the query search result
	 * @throws RebirthException the rebirth exception
	 */
	public QuerySearchResult executeScan(InternalSearchRequest request) throws RebirthException {
		SearchContext context = createContext(request);
		assert context.searchType() == SearchType.SCAN;
		context.searchType(SearchType.COUNT);
		activeContexts.put(context.id(), context);
		assert context.searchType() == SearchType.COUNT;
		try {
			if (context.scroll() == null) {
				throw new RebirthException("Scroll must be provided when scanning...");
			}
			contextProcessing(context);
			queryPhase.execute(context);
			contextProcessedSuccessfully(context);
			return context.queryResult();
		} catch (RuntimeException e) {
			logger.trace("Scan phase failed", e);
			freeContext(context);
			throw e;
		} finally {
			cleanContext(context);
		}
	}

	/**
	 * Execute scan.
	 *
	 * @param request the request
	 * @return the scroll query fetch search result
	 * @throws RebirthException the rebirth exception
	 */
	public ScrollQueryFetchSearchResult executeScan(InternalScrollSearchRequest request) throws RebirthException {
		SearchContext context = findContext(request.id());
		contextProcessing(context);
		try {
			processScroll(request, context);
			if (context.searchType() == SearchType.COUNT) {

				context.searchType(SearchType.SCAN);
				context.from(0);
			}
			queryPhase.execute(context);
			shortcutDocIdsToLoadForScanning(context);
			fetchPhase.execute(context);
			if (context.scroll() == null || context.fetchResult().hits().hits().length < context.size()) {
				freeContext(request.id());
			} else {
				contextProcessedSuccessfully(context);
			}
			return new ScrollQueryFetchSearchResult(new QueryFetchSearchResult(context.queryResult(),
					context.fetchResult()), context.shardTarget());
		} catch (RuntimeException e) {
			logger.trace("Scan phase failed", e);
			freeContext(context);
			throw e;
		} finally {
			cleanContext(context);
		}
	}

	/**
	 * Execute query phase.
	 *
	 * @param request the request
	 * @return the query search result
	 * @throws RebirthException the rebirth exception
	 */
	public QuerySearchResult executeQueryPhase(InternalSearchRequest request) throws RebirthException {
		SearchContext context = createContext(request);
		activeContexts.put(context.id(), context);
		try {
			context.indexShard().searchService().onPreQueryPhase(context);
			long time = System.nanoTime();
			contextProcessing(context);
			queryPhase.execute(context);
			if (context.searchType() == SearchType.COUNT) {
				freeContext(context.id());
			} else {
				contextProcessedSuccessfully(context);
			}
			context.indexShard().searchService().onQueryPhase(context, System.nanoTime() - time);
			return context.queryResult();
		} catch (RuntimeException e) {
			context.indexShard().searchService().onFailedQueryPhase(context);
			logger.trace("Query phase failed", e);
			freeContext(context);
			throw e;
		} finally {
			cleanContext(context);
		}
	}

	/**
	 * Execute query phase.
	 *
	 * @param request the request
	 * @return the scroll query search result
	 * @throws RebirthException the rebirth exception
	 */
	public ScrollQuerySearchResult executeQueryPhase(InternalScrollSearchRequest request) throws RebirthException {
		SearchContext context = findContext(request.id());
		try {
			context.indexShard().searchService().onPreQueryPhase(context);
			long time = System.nanoTime();
			contextProcessing(context);
			processScroll(request, context);
			queryPhase.execute(context);
			contextProcessedSuccessfully(context);
			context.indexShard().searchService().onQueryPhase(context, System.nanoTime() - time);
			return new ScrollQuerySearchResult(context.queryResult(), context.shardTarget());
		} catch (RuntimeException e) {
			context.indexShard().searchService().onFailedQueryPhase(context);
			logger.trace("Query phase failed", e);
			freeContext(context);
			throw e;
		} finally {
			cleanContext(context);
		}
	}

	/**
	 * Execute query phase.
	 *
	 * @param request the request
	 * @return the query search result
	 * @throws RebirthException the rebirth exception
	 */
	public QuerySearchResult executeQueryPhase(QuerySearchRequest request) throws RebirthException {
		SearchContext context = findContext(request.id());
		contextProcessing(context);
		try {
			context.searcher().dfSource(
					new CachedDfSource(request.dfs(), context.similarityService().defaultSearchSimilarity()));
		} catch (IOException e) {
			freeContext(context);
			cleanContext(context);
			throw new QueryPhaseExecutionException(context, "Failed to set aggregated df", e);
		}
		try {
			context.indexShard().searchService().onPreQueryPhase(context);
			long time = System.nanoTime();
			queryPhase.execute(context);
			contextProcessedSuccessfully(context);
			context.indexShard().searchService().onQueryPhase(context, System.nanoTime() - time);
			return context.queryResult();
		} catch (RuntimeException e) {
			context.indexShard().searchService().onFailedQueryPhase(context);
			logger.trace("Query phase failed", e);
			freeContext(context);
			throw e;
		} finally {
			cleanContext(context);
		}
	}

	/**
	 * Execute fetch phase.
	 *
	 * @param request the request
	 * @return the query fetch search result
	 * @throws RebirthException the rebirth exception
	 */
	public QueryFetchSearchResult executeFetchPhase(InternalSearchRequest request) throws RebirthException {
		SearchContext context = createContext(request);
		activeContexts.put(context.id(), context);
		contextProcessing(context);
		try {
			context.indexShard().searchService().onPreQueryPhase(context);
			long time = System.nanoTime();
			try {
				queryPhase.execute(context);
			} catch (RuntimeException e) {
				context.indexShard().searchService().onFailedQueryPhase(context);
				throw e;
			}
			long time2 = System.nanoTime();
			context.indexShard().searchService().onQueryPhase(context, time2 - time);
			context.indexShard().searchService().onPreFetchPhase(context);
			try {
				shortcutDocIdsToLoad(context);
				fetchPhase.execute(context);
				if (context.scroll() == null) {
					freeContext(context.id());
				} else {
					contextProcessedSuccessfully(context);
				}
			} catch (RuntimeException e) {
				context.indexShard().searchService().onFailedFetchPhase(context);
				throw e;
			}
			context.indexShard().searchService().onFetchPhase(context, System.nanoTime() - time2);
			return new QueryFetchSearchResult(context.queryResult(), context.fetchResult());
		} catch (RuntimeException e) {
			logger.trace("Fetch phase failed", e);
			freeContext(context);
			throw e;
		} finally {
			cleanContext(context);
		}
	}

	/**
	 * Execute fetch phase.
	 *
	 * @param request the request
	 * @return the query fetch search result
	 * @throws RebirthException the rebirth exception
	 */
	public QueryFetchSearchResult executeFetchPhase(QuerySearchRequest request) throws RebirthException {
		SearchContext context = findContext(request.id());
		contextProcessing(context);
		try {
			context.searcher().dfSource(
					new CachedDfSource(request.dfs(), context.similarityService().defaultSearchSimilarity()));
		} catch (IOException e) {
			freeContext(context);
			cleanContext(context);
			throw new QueryPhaseExecutionException(context, "Failed to set aggregated df", e);
		}
		try {
			context.indexShard().searchService().onPreQueryPhase(context);
			long time = System.nanoTime();
			try {
				queryPhase.execute(context);
			} catch (RuntimeException e) {
				context.indexShard().searchService().onFailedQueryPhase(context);
				throw e;
			}
			long time2 = System.nanoTime();
			context.indexShard().searchService().onQueryPhase(context, time2 - time);
			context.indexShard().searchService().onPreFetchPhase(context);
			try {
				shortcutDocIdsToLoad(context);
				fetchPhase.execute(context);
				if (context.scroll() == null) {
					freeContext(request.id());
				} else {
					contextProcessedSuccessfully(context);
				}
			} catch (RuntimeException e) {
				context.indexShard().searchService().onFailedFetchPhase(context);
				throw e;
			}
			context.indexShard().searchService().onFetchPhase(context, System.nanoTime() - time2);
			return new QueryFetchSearchResult(context.queryResult(), context.fetchResult());
		} catch (RuntimeException e) {
			logger.trace("Fetch phase failed", e);
			freeContext(context);
			throw e;
		} finally {
			cleanContext(context);
		}
	}

	/**
	 * Execute fetch phase.
	 *
	 * @param request the request
	 * @return the scroll query fetch search result
	 * @throws RebirthException the rebirth exception
	 */
	public ScrollQueryFetchSearchResult executeFetchPhase(InternalScrollSearchRequest request) throws RebirthException {
		SearchContext context = findContext(request.id());
		contextProcessing(context);
		try {
			processScroll(request, context);
			context.indexShard().searchService().onPreQueryPhase(context);
			long time = System.nanoTime();
			try {
				queryPhase.execute(context);
			} catch (RuntimeException e) {
				context.indexShard().searchService().onFailedQueryPhase(context);
				throw e;
			}
			long time2 = System.nanoTime();
			context.indexShard().searchService().onQueryPhase(context, time2 - time);
			context.indexShard().searchService().onPreFetchPhase(context);
			try {
				shortcutDocIdsToLoad(context);
				fetchPhase.execute(context);
				if (context.scroll() == null) {
					freeContext(request.id());
				} else {
					contextProcessedSuccessfully(context);
				}
			} catch (RuntimeException e) {
				context.indexShard().searchService().onFailedFetchPhase(context);
				throw e;
			}
			context.indexShard().searchService().onFetchPhase(context, System.nanoTime() - time2);
			return new ScrollQueryFetchSearchResult(new QueryFetchSearchResult(context.queryResult(),
					context.fetchResult()), context.shardTarget());
		} catch (RuntimeException e) {
			logger.trace("Fetch phase failed", e);
			freeContext(context);
			throw e;
		} finally {
			cleanContext(context);
		}
	}

	/**
	 * Execute fetch phase.
	 *
	 * @param request the request
	 * @return the fetch search result
	 * @throws RebirthException the rebirth exception
	 */
	public FetchSearchResult executeFetchPhase(FetchSearchRequest request) throws RebirthException {
		SearchContext context = findContext(request.id());
		contextProcessing(context);
		try {
			context.docIdsToLoad(request.docIds(), 0, request.docIdsSize());
			context.indexShard().searchService().onPreFetchPhase(context);
			long time = System.nanoTime();
			fetchPhase.execute(context);
			if (context.scroll() == null) {
				freeContext(request.id());
			} else {
				contextProcessedSuccessfully(context);
			}
			context.indexShard().searchService().onFetchPhase(context, System.nanoTime() - time);
			return context.fetchResult();
		} catch (RuntimeException e) {
			context.indexShard().searchService().onFailedFetchPhase(context);
			logger.trace("Fetch phase failed", e);
			freeContext(context);
			throw e;
		} finally {
			cleanContext(context);
		}
	}

	/**
	 * Find context.
	 *
	 * @param id the id
	 * @return the search context
	 * @throws SearchContextMissingException the search context missing exception
	 */
	private SearchContext findContext(long id) throws SearchContextMissingException {
		SearchContext context = activeContexts.get(id);
		if (context == null) {
			throw new SearchContextMissingException(id);
		}
		SearchContext.setCurrent(context);
		return context;
	}

	/**
	 * Creates the context.
	 *
	 * @param request the request
	 * @return the search context
	 * @throws RebirthException the rebirth exception
	 */
	private SearchContext createContext(InternalSearchRequest request) throws RebirthException {
		IndexService indexService = indicesService.indexServiceSafe(request.index());
		IndexShard indexShard = indexService.shardSafe(request.shardId());

		SearchShardTarget shardTarget = new SearchShardTarget(clusterService.localNode().id(), request.index(),
				request.shardId());

		Engine.Searcher engineSearcher = indexShard.searcher();
		SearchContext context = new SearchContext(idGenerator.incrementAndGet(), request, shardTarget, engineSearcher,
				indexService, indexShard, scriptService);
		SearchContext.setCurrent(context);
		try {
			context.scroll(request.scroll());

			parseSource(context, request.source(), request.sourceOffset(), request.sourceLength());
			parseSource(context, request.extraSource(), request.extraSourceOffset(), request.extraSourceLength());

			if (context.from() == -1) {
				context.from(0);
			}
			if (context.size() == -1) {
				context.size(10);
			}

			Filter aliasFilter = indexService.aliasesService().aliasFilter(request.filteringAliases());
			context.aliasFilter(aliasFilter);

			dfsPhase.preProcess(context);
			queryPhase.preProcess(context);
			fetchPhase.preProcess(context);

			long keepAlive = defaultKeepAlive;
			if (request.scroll() != null && request.scroll().keepAlive() != null) {
				keepAlive = request.scroll().keepAlive().millis();
			}
			context.keepAlive(keepAlive);
		} catch (RuntimeException e) {
			context.release();
			throw e;
		}

		return context;
	}

	/**
	 * Free context.
	 *
	 * @param id the id
	 */
	public void freeContext(long id) {
		SearchContext context = activeContexts.remove(id);
		if (context == null) {
			return;
		}
		freeContext(context);
	}

	/**
	 * Free context.
	 *
	 * @param context the context
	 */
	private void freeContext(SearchContext context) {
		activeContexts.remove(context.id());
		context.release();
	}

	/**
	 * Context processing.
	 *
	 * @param context the context
	 */
	private void contextProcessing(SearchContext context) {

		context.accessed(-1);
	}

	/**
	 * Context processed successfully.
	 *
	 * @param context the context
	 */
	private void contextProcessedSuccessfully(SearchContext context) {
		context.accessed(threadPool.estimatedTimeInMillis());
	}

	/**
	 * Clean context.
	 *
	 * @param context the context
	 */
	private void cleanContext(SearchContext context) {
		SearchContext.removeCurrent();
	}

	/**
	 * Parses the source.
	 *
	 * @param context the context
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @throws SearchParseException the search parse exception
	 */
	private void parseSource(SearchContext context, byte[] source, int offset, int length) throws SearchParseException {

		if (source == null || length == 0) {
			return;
		}
		XContentParser parser = null;
		try {
			parser = XContentFactory.xContent(source, offset, length).createParser(source, offset, length);
			XContentParser.Token token;
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					String fieldName = parser.currentName();
					parser.nextToken();
					SearchParseElement element = elementParsers.get(fieldName);
					if (element == null) {
						throw new SearchParseException(context, "No parser for element [" + fieldName + "]");
					}
					element.parse(parser, context);
				} else if (token == null) {
					break;
				}
			}
		} catch (Exception e) {
			String sSource = "_na_";
			try {
				sSource = Unicode.fromBytes(source, offset, length);
			} catch (Throwable e1) {

			}
			throw new SearchParseException(context, "Failed to parse source [" + sSource + "]", e);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
	}

	/** The Constant EMPTY_DOC_IDS. */
	private static final int[] EMPTY_DOC_IDS = new int[0];

	/**
	 * Shortcut doc ids to load.
	 *
	 * @param context the context
	 */
	private void shortcutDocIdsToLoad(SearchContext context) {
		TopDocs topDocs = context.queryResult().topDocs();
		if (topDocs.scoreDocs.length < context.from()) {

			context.docIdsToLoad(EMPTY_DOC_IDS, 0, 0);
			return;
		}
		int totalSize = context.from() + context.size();
		int[] docIdsToLoad = new int[context.size()];
		int counter = 0;
		for (int i = context.from(); i < totalSize; i++) {
			if (i < topDocs.scoreDocs.length) {
				docIdsToLoad[counter] = topDocs.scoreDocs[i].doc;
			} else {
				break;
			}
			counter++;
		}
		context.docIdsToLoad(docIdsToLoad, 0, counter);
	}

	/**
	 * Shortcut doc ids to load for scanning.
	 *
	 * @param context the context
	 */
	private void shortcutDocIdsToLoadForScanning(SearchContext context) {
		TopDocs topDocs = context.queryResult().topDocs();
		if (topDocs.scoreDocs.length == 0) {

			context.docIdsToLoad(EMPTY_DOC_IDS, 0, 0);
			return;
		}
		int[] docIdsToLoad = new int[topDocs.scoreDocs.length];
		for (int i = 0; i < docIdsToLoad.length; i++) {
			docIdsToLoad[i] = topDocs.scoreDocs[i].doc;
		}
		context.docIdsToLoad(docIdsToLoad, 0, docIdsToLoad.length);
	}

	/**
	 * Process scroll.
	 *
	 * @param request the request
	 * @param context the context
	 */
	private void processScroll(InternalScrollSearchRequest request, SearchContext context) {

		context.from(context.from() + context.size());
		context.scroll(request.scroll());

		if (request.scroll() != null && request.scroll().keepAlive() != null) {
			context.keepAlive(request.scroll().keepAlive().millis());
		}
	}

	/**
	 * The listener interface for receiving cleanContextOnIndicesLifecycle events.
	 * The class that is interested in processing a cleanContextOnIndicesLifecycle
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addCleanContextOnIndicesLifecycleListener<code> method. When
	 * the cleanContextOnIndicesLifecycle event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see CleanContextOnIndicesLifecycleEvent
	 */
	class CleanContextOnIndicesLifecycleListener extends IndicesLifecycle.Listener {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.indices.IndicesLifecycle.Listener#beforeIndexClosed(cn.com.rebirth.search.core.index.service.IndexService, boolean)
		 */
		@Override
		public void beforeIndexClosed(IndexService indexService, boolean delete) {
			releaseContextsForIndex(indexService.index());
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.indices.IndicesLifecycle.Listener#beforeIndexShardClosed(cn.com.rebirth.search.core.index.shard.ShardId, cn.com.rebirth.search.core.index.shard.service.IndexShard, boolean)
		 */
		@Override
		public void beforeIndexShardClosed(ShardId shardId, @Nullable IndexShard indexShard, boolean delete) {
			releaseContextsForShard(shardId);
		}
	}

	/**
	 * The Class Reaper.
	 *
	 * @author l.xue.nong
	 */
	class Reaper implements Runnable {

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			long time = threadPool.estimatedTimeInMillis();
			for (SearchContext context : activeContexts.values()) {
				if (context.lastAccessTime() == -1) {
					continue;
				}
				if ((time - context.lastAccessTime() > context.keepAlive())) {
					freeContext(context);
				}
			}
		}
	}
}
