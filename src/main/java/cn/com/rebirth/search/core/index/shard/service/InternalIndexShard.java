/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalIndexShard.java 2012-7-6 14:29:42 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard.service;

import static cn.com.rebirth.search.core.index.mapper.SourceToParse.source;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.ThreadInterruptedException;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.FastByteArrayOutputStream;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.commons.lucene.search.Queries;
import cn.com.rebirth.search.commons.metrics.MeanMetric;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.index.aliases.IndexAliasesService;
import cn.com.rebirth.search.core.index.cache.IndexCache;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.engine.EngineClosedException;
import cn.com.rebirth.search.core.index.engine.EngineException;
import cn.com.rebirth.search.core.index.engine.IgnoreOnRecoveryEngineException;
import cn.com.rebirth.search.core.index.engine.OptimizeFailedEngineException;
import cn.com.rebirth.search.core.index.engine.RefreshFailedEngineException;
import cn.com.rebirth.search.core.index.flush.FlushStats;
import cn.com.rebirth.search.core.index.get.GetStats;
import cn.com.rebirth.search.core.index.get.ShardGetService;
import cn.com.rebirth.search.core.index.indexing.IndexingStats;
import cn.com.rebirth.search.core.index.indexing.ShardIndexingService;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.ParsedDocument;
import cn.com.rebirth.search.core.index.mapper.SourceToParse;
import cn.com.rebirth.search.core.index.mapper.Uid;
import cn.com.rebirth.search.core.index.merge.MergeStats;
import cn.com.rebirth.search.core.index.merge.scheduler.MergeSchedulerProvider;
import cn.com.rebirth.search.core.index.query.IndexQueryParserService;
import cn.com.rebirth.search.core.index.query.QueryParseContext;
import cn.com.rebirth.search.core.index.refresh.RefreshStats;
import cn.com.rebirth.search.core.index.search.nested.IncludeAllChildrenQuery;
import cn.com.rebirth.search.core.index.search.nested.NonNestedDocsFilter;
import cn.com.rebirth.search.core.index.search.stats.SearchStats;
import cn.com.rebirth.search.core.index.search.stats.ShardSearchService;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.settings.IndexSettingsService;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.DocsStats;
import cn.com.rebirth.search.core.index.shard.IllegalIndexShardStateException;
import cn.com.rebirth.search.core.index.shard.IndexShardClosedException;
import cn.com.rebirth.search.core.index.shard.IndexShardException;
import cn.com.rebirth.search.core.index.shard.IndexShardNotRecoveringException;
import cn.com.rebirth.search.core.index.shard.IndexShardNotStartedException;
import cn.com.rebirth.search.core.index.shard.IndexShardRecoveringException;
import cn.com.rebirth.search.core.index.shard.IndexShardRelocatedException;
import cn.com.rebirth.search.core.index.shard.IndexShardStartedException;
import cn.com.rebirth.search.core.index.shard.IndexShardState;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.store.Store;
import cn.com.rebirth.search.core.index.store.StoreStats;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.indices.IndicesLifecycle;
import cn.com.rebirth.search.core.indices.InternalIndicesLifecycle;
import cn.com.rebirth.search.core.indices.recovery.RecoveryStatus;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

/**
 * The Class InternalIndexShard.
 *
 * @author l.xue.nong
 */
public class InternalIndexShard extends AbstractIndexShardComponent implements IndexShard {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The index settings service. */
	private final IndexSettingsService indexSettingsService;

	/** The mapper service. */
	private final MapperService mapperService;

	/** The query parser service. */
	private final IndexQueryParserService queryParserService;

	/** The index cache. */
	private final IndexCache indexCache;

	/** The indices lifecycle. */
	private final InternalIndicesLifecycle indicesLifecycle;

	/** The store. */
	private final Store store;

	/** The merge scheduler. */
	private final MergeSchedulerProvider mergeScheduler;

	/** The engine. */
	private final Engine engine;

	/** The translog. */
	private final Translog translog;

	/** The index aliases service. */
	private final IndexAliasesService indexAliasesService;

	/** The indexing service. */
	private final ShardIndexingService indexingService;

	/** The search service. */
	private final ShardSearchService searchService;

	/** The get service. */
	private final ShardGetService getService;

	/** The mutex. */
	private final Object mutex = new Object();

	/** The check index on startup. */
	private final boolean checkIndexOnStartup;

	/** The check index took. */
	private long checkIndexTook = 0;

	/** The state. */
	private volatile IndexShardState state;

	/** The refresh interval. */
	private TimeValue refreshInterval;

	/** The merge interval. */
	private final TimeValue mergeInterval;

	/** The refresh scheduled future. */
	private volatile ScheduledFuture refreshScheduledFuture;

	/** The merge schedule future. */
	private volatile ScheduledFuture mergeScheduleFuture;

	/** The shard routing. */
	private volatile ShardRouting shardRouting;

	/** The peer recovery status. */
	private RecoveryStatus peerRecoveryStatus;

	/** The apply refresh settings. */
	private ApplyRefreshSettings applyRefreshSettings = new ApplyRefreshSettings();

	/** The refresh metric. */
	private final MeanMetric refreshMetric = new MeanMetric();

	/** The flush metric. */
	private final MeanMetric flushMetric = new MeanMetric();

	/**
	 * Instantiates a new internal index shard.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param indexSettingsService the index settings service
	 * @param indicesLifecycle the indices lifecycle
	 * @param store the store
	 * @param engine the engine
	 * @param mergeScheduler the merge scheduler
	 * @param translog the translog
	 * @param threadPool the thread pool
	 * @param mapperService the mapper service
	 * @param queryParserService the query parser service
	 * @param indexCache the index cache
	 * @param indexAliasesService the index aliases service
	 * @param indexingService the indexing service
	 * @param getService the get service
	 * @param searchService the search service
	 */
	@Inject
	public InternalIndexShard(ShardId shardId, @IndexSettings Settings indexSettings,
			IndexSettingsService indexSettingsService, IndicesLifecycle indicesLifecycle, Store store, Engine engine,
			MergeSchedulerProvider mergeScheduler, Translog translog, ThreadPool threadPool,
			MapperService mapperService, IndexQueryParserService queryParserService, IndexCache indexCache,
			IndexAliasesService indexAliasesService, ShardIndexingService indexingService, ShardGetService getService,
			ShardSearchService searchService) {
		super(shardId, indexSettings);
		this.indicesLifecycle = (InternalIndicesLifecycle) indicesLifecycle;
		this.indexSettingsService = indexSettingsService;
		this.store = store;
		this.engine = engine;
		this.mergeScheduler = mergeScheduler;
		this.translog = translog;
		this.threadPool = threadPool;
		this.mapperService = mapperService;
		this.queryParserService = queryParserService;
		this.indexCache = indexCache;
		this.indexAliasesService = indexAliasesService;
		this.indexingService = indexingService;
		this.getService = getService.setIndexShard(this);
		this.searchService = searchService;
		state = IndexShardState.CREATED;

		this.refreshInterval = indexSettings.getAsTime("engine.robin.refresh_interval",
				indexSettings.getAsTime("index.refresh_interval", engine.defaultRefreshInterval()));
		this.mergeInterval = indexSettings.getAsTime("index.merge.async_interval", TimeValue.timeValueSeconds(1));

		indexSettingsService.addListener(applyRefreshSettings);

		logger.debug("state: [CREATED]");

		this.checkIndexOnStartup = indexSettings.getAsBoolean("index.shard.check_on_startup", false);
	}

	/**
	 * Merge scheduler.
	 *
	 * @return the merge scheduler provider
	 */
	public MergeSchedulerProvider mergeScheduler() {
		return this.mergeScheduler;
	}

	/**
	 * Store.
	 *
	 * @return the store
	 */
	public Store store() {
		return this.store;
	}

	/**
	 * Engine.
	 *
	 * @return the engine
	 */
	public Engine engine() {
		return engine;
	}

	/**
	 * Translog.
	 *
	 * @return the translog
	 */
	public Translog translog() {
		return translog;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#indexingService()
	 */
	public ShardIndexingService indexingService() {
		return this.indexingService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#getService()
	 */
	@Override
	public ShardGetService getService() {
		return this.getService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#searchService()
	 */
	@Override
	public ShardSearchService searchService() {
		return this.searchService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#routingEntry()
	 */
	@Override
	public ShardRouting routingEntry() {
		return this.shardRouting;
	}

	/**
	 * Routing entry.
	 *
	 * @param shardRouting the shard routing
	 * @return the internal index shard
	 */
	public InternalIndexShard routingEntry(ShardRouting shardRouting) {
		ShardRouting currentRouting = this.shardRouting;
		if (!shardRouting.shardId().equals(shardId())) {
			throw new RebirthIllegalArgumentException("Trying to set a routing entry with shardId ["
					+ shardRouting.shardId() + "] on a shard with shardId [" + shardId() + "]");
		}
		if (currentRouting != null) {
			if (!shardRouting.primary() && currentRouting.primary()) {
				logger.warn("suspect illegal state: trying to move shard from primary mode to backup mode");
			}

			if (currentRouting.equals(shardRouting)) {
				return this;
			}
		}
		this.shardRouting = shardRouting;
		indicesLifecycle.shardRoutingChanged(this, currentRouting, shardRouting);
		return this;
	}

	/**
	 * Recovering.
	 *
	 * @param reason the reason
	 * @return the index shard state
	 * @throws IndexShardStartedException the index shard started exception
	 * @throws IndexShardRelocatedException the index shard relocated exception
	 * @throws IndexShardRecoveringException the index shard recovering exception
	 * @throws IndexShardClosedException the index shard closed exception
	 */
	public IndexShardState recovering(String reason) throws IndexShardStartedException, IndexShardRelocatedException,
			IndexShardRecoveringException, IndexShardClosedException {
		synchronized (mutex) {
			IndexShardState returnValue = state;
			if (state == IndexShardState.CLOSED) {
				throw new IndexShardClosedException(shardId);
			}
			if (state == IndexShardState.STARTED) {
				throw new IndexShardStartedException(shardId);
			}
			if (state == IndexShardState.RELOCATED) {
				throw new IndexShardRelocatedException(shardId);
			}
			if (state == IndexShardState.RECOVERING) {
				throw new IndexShardRecoveringException(shardId);
			}
			logger.debug("state: [" + state + "]->[" + IndexShardState.RECOVERING + "], reason [" + reason + "]");
			state = IndexShardState.RECOVERING;
			return returnValue;
		}
	}

	/**
	 * Relocated.
	 *
	 * @param reason the reason
	 * @return the internal index shard
	 * @throws IndexShardNotStartedException the index shard not started exception
	 */
	public InternalIndexShard relocated(String reason) throws IndexShardNotStartedException {
		synchronized (mutex) {
			if (state != IndexShardState.STARTED) {
				throw new IndexShardNotStartedException(shardId, state);
			}
			logger.debug("state: [" + state + "]->[" + IndexShardState.RELOCATED + "], reason [" + reason + "]");
			state = IndexShardState.RELOCATED;
		}
		return this;
	}

	/**
	 * Start.
	 *
	 * @param reason the reason
	 * @return the internal index shard
	 * @throws IndexShardStartedException the index shard started exception
	 * @throws IndexShardRelocatedException the index shard relocated exception
	 * @throws IndexShardClosedException the index shard closed exception
	 */
	public InternalIndexShard start(String reason) throws IndexShardStartedException, IndexShardRelocatedException,
			IndexShardClosedException {
		synchronized (mutex) {
			if (state == IndexShardState.CLOSED) {
				throw new IndexShardClosedException(shardId);
			}
			if (state == IndexShardState.STARTED) {
				throw new IndexShardStartedException(shardId);
			}
			if (state == IndexShardState.RELOCATED) {
				throw new IndexShardRelocatedException(shardId);
			}
			if (checkIndexOnStartup) {
				checkIndex(true);
			}
			engine.start();
			startScheduledTasksIfNeeded();
			logger.debug("state: [" + state + "]->[" + IndexShardState.STARTED + "], reason [" + reason + "]");
			state = IndexShardState.STARTED;
		}
		indicesLifecycle.afterIndexShardStarted(this);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#state()
	 */
	@Override
	public IndexShardState state() {
		return state;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#prepareCreate(cn.com.rebirth.search.core.index.mapper.SourceToParse)
	 */
	@Override
	public Engine.Create prepareCreate(SourceToParse source) throws RebirthException {
		long startTime = System.nanoTime();
		DocumentMapper docMapper = mapperService.documentMapperWithAutoCreate(source.type());
		ParsedDocument doc = docMapper.parse(source);
		return new Engine.Create(docMapper, docMapper.uidMapper().term(doc.uid()), doc).startTime(startTime);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#create(cn.com.rebirth.search.core.index.engine.Engine.Create)
	 */
	@Override
	public ParsedDocument create(Engine.Create create) throws RebirthException {
		writeAllowed();
		create = indexingService.preCreate(create);
		if (logger.isTraceEnabled()) {
			logger.trace("index {}", create.docs());
		}
		engine.create(create);
		create.endTime(System.nanoTime());
		indexingService.postCreate(create);
		return create.parsedDoc();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#prepareIndex(cn.com.rebirth.search.core.index.mapper.SourceToParse)
	 */
	@Override
	public Engine.Index prepareIndex(SourceToParse source) throws RebirthException {
		long startTime = System.nanoTime();
		DocumentMapper docMapper = mapperService.documentMapperWithAutoCreate(source.type());
		ParsedDocument doc = docMapper.parse(source);
		return new Engine.Index(docMapper, docMapper.uidMapper().term(doc.uid()), doc).startTime(startTime);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#index(cn.com.rebirth.search.core.index.engine.Engine.Index)
	 */
	@Override
	public ParsedDocument index(Engine.Index index) throws RebirthException {
		writeAllowed();
		index = indexingService.preIndex(index);
		try {
			if (logger.isTraceEnabled()) {
				logger.trace("index {}", index.docs());
			}
			engine.index(index);
			index.endTime(System.nanoTime());
		} catch (RuntimeException ex) {
			indexingService.failedIndex(index);
			throw ex;
		}
		indexingService.postIndex(index);
		return index.parsedDoc();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#prepareDelete(java.lang.String, java.lang.String, long)
	 */
	@Override
	public Engine.Delete prepareDelete(String type, String id, long version) throws RebirthException {
		long startTime = System.nanoTime();
		DocumentMapper docMapper = mapperService.documentMapperWithAutoCreate(type);
		return new Engine.Delete(type, id, docMapper.uidMapper().term(type, id)).version(version).startTime(startTime);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#delete(cn.com.rebirth.search.core.index.engine.Engine.Delete)
	 */
	@Override
	public void delete(Engine.Delete delete) throws RebirthException {
		writeAllowed();
		delete = indexingService.preDelete(delete);
		try {
			if (logger.isTraceEnabled()) {
				logger.trace("delete [{}]", delete.uid().text());
			}
			engine.delete(delete);
			delete.endTime(System.nanoTime());
		} catch (RuntimeException ex) {
			indexingService.failedDelete(delete);
			throw ex;
		}
		indexingService.postDelete(delete);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#prepareDeleteByQuery(cn.com.rebirth.commons.BytesHolder, java.lang.String[], java.lang.String[])
	 */
	@Override
	public Engine.DeleteByQuery prepareDeleteByQuery(BytesHolder querySource, @Nullable String[] filteringAliases,
			String... types) throws RebirthException {
		long startTime = System.nanoTime();
		if (types == null) {
			types = Strings.EMPTY_ARRAY;
		}
		Query query = queryParserService.parse(querySource.bytes(), querySource.offset(), querySource.length()).query();
		query = filterQueryIfNeeded(query, types);

		Filter aliasFilter = indexAliasesService.aliasFilter(filteringAliases);

		return new Engine.DeleteByQuery(query, querySource, filteringAliases, aliasFilter, types).startTime(startTime);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#deleteByQuery(cn.com.rebirth.search.core.index.engine.Engine.DeleteByQuery)
	 */
	@Override
	public void deleteByQuery(Engine.DeleteByQuery deleteByQuery) throws RebirthException {
		writeAllowed();
		if (mapperService.hasNested()) {

			IncludeAllChildrenQuery nestedQuery = new IncludeAllChildrenQuery(deleteByQuery.query(), indexCache
					.filter().cache(NonNestedDocsFilter.INSTANCE));
			deleteByQuery = new Engine.DeleteByQuery(nestedQuery, deleteByQuery.source(),
					deleteByQuery.filteringAliases(), deleteByQuery.aliasFilter(), deleteByQuery.types());
		}
		if (logger.isTraceEnabled()) {
			logger.trace("delete_by_query [{}]", deleteByQuery.query());
		}
		deleteByQuery = indexingService.preDeleteByQuery(deleteByQuery);
		engine.delete(deleteByQuery);
		deleteByQuery.endTime(System.nanoTime());
		indexingService.postDeleteByQuery(deleteByQuery);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#get(cn.com.rebirth.search.core.index.engine.Engine.Get)
	 */
	@Override
	public Engine.GetResult get(Engine.Get get) throws RebirthException {
		readAllowed();
		return engine.get(get);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#count(float, byte[], java.lang.String[], java.lang.String[])
	 */
	@Override
	public long count(float minScore, byte[] querySource, @Nullable String[] filteringAliases, String... types)
			throws RebirthException {
		return count(minScore, querySource, 0, querySource.length, filteringAliases, types);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#count(float, byte[], int, int, java.lang.String[], java.lang.String[])
	 */
	@Override
	public long count(float minScore, byte[] querySource, int querySourceOffset, int querySourceLength,
			@Nullable String[] filteringAliases, String... types) throws RebirthException {
		readAllowed();
		Query query;
		if (querySourceLength == 0) {
			query = Queries.MATCH_ALL_QUERY;
		} else {
			try {
				QueryParseContext.setTypes(types);
				query = queryParserService.parse(querySource, querySourceOffset, querySourceLength).query();
			} finally {
				QueryParseContext.removeTypes();
			}
		}

		query = filterQueryIfNeeded(query, types);
		Filter aliasFilter = indexAliasesService.aliasFilter(filteringAliases);
		Engine.Searcher searcher = engine.searcher();
		try {
			long count = Lucene.count(searcher.searcher(), query, aliasFilter, minScore);
			if (logger.isTraceEnabled()) {
				logger.trace("count of [{}] is [{}]", query, count);
			}
			return count;
		} catch (IOException e) {
			throw new RebirthException("Failed to count query [" + query + "]", e);
		} finally {
			searcher.release();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#refresh(cn.com.rebirth.search.core.index.engine.Engine.Refresh)
	 */
	@Override
	public void refresh(Engine.Refresh refresh) throws RebirthException {
		verifyStarted();
		if (logger.isTraceEnabled()) {
			logger.trace("refresh with {}", refresh);
		}
		long time = System.nanoTime();
		engine.refresh(refresh);
		refreshMetric.inc(System.nanoTime() - time);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#refreshStats()
	 */
	@Override
	public RefreshStats refreshStats() {
		return new RefreshStats(refreshMetric.count(), TimeUnit.NANOSECONDS.toMillis(refreshMetric.sum()));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#flushStats()
	 */
	@Override
	public FlushStats flushStats() {
		return new FlushStats(flushMetric.count(), TimeUnit.NANOSECONDS.toMillis(flushMetric.sum()));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#docStats()
	 */
	@Override
	public DocsStats docStats() {
		Engine.Searcher searcher = null;
		try {
			searcher = engine.searcher();
			return new DocsStats(searcher.reader().numDocs(), searcher.reader().numDeletedDocs());
		} catch (Exception e) {
			return new DocsStats();
		} finally {
			if (searcher != null) {
				searcher.release();
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#indexingStats(java.lang.String[])
	 */
	@Override
	public IndexingStats indexingStats(String... types) {
		return indexingService.stats(types);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#searchStats(java.lang.String[])
	 */
	@Override
	public SearchStats searchStats(String... groups) {
		return searchService.stats(groups);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#getStats()
	 */
	@Override
	public GetStats getStats() {
		return getService.stats();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#storeStats()
	 */
	@Override
	public StoreStats storeStats() {
		try {
			return store.stats();
		} catch (IOException e) {
			return new StoreStats();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#mergeStats()
	 */
	@Override
	public MergeStats mergeStats() {
		return mergeScheduler.stats();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#flush(cn.com.rebirth.search.core.index.engine.Engine.Flush)
	 */
	@Override
	public void flush(Engine.Flush flush) throws RebirthException {
		verifyStartedOrRecovering();
		if (logger.isTraceEnabled()) {
			logger.trace("flush with {}", flush);
		}
		long time = System.nanoTime();
		engine.flush(flush);
		flushMetric.inc(System.nanoTime() - time);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#optimize(cn.com.rebirth.search.core.index.engine.Engine.Optimize)
	 */
	@Override
	public void optimize(Engine.Optimize optimize) throws RebirthException {
		verifyStarted();
		if (logger.isTraceEnabled()) {
			logger.trace("optimize with {}", optimize);
		}
		engine.optimize(optimize);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#snapshot(cn.com.rebirth.search.core.index.engine.Engine.SnapshotHandler)
	 */
	@Override
	public <T> T snapshot(Engine.SnapshotHandler<T> snapshotHandler) throws EngineException {
		IndexShardState state = this.state;

		if (state != IndexShardState.STARTED && state != IndexShardState.RELOCATED && state != IndexShardState.CLOSED) {
			throw new IllegalIndexShardStateException(shardId, state, "snapshot is not allowed");
		}
		return engine.snapshot(snapshotHandler);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#recover(cn.com.rebirth.search.core.index.engine.Engine.RecoveryHandler)
	 */
	@Override
	public void recover(Engine.RecoveryHandler recoveryHandler) throws EngineException {
		verifyStarted();
		engine.recover(recoveryHandler);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#searcher()
	 */
	@Override
	public Engine.Searcher searcher() {
		readAllowed();
		return engine.searcher();
	}

	/**
	 * Close.
	 *
	 * @param reason the reason
	 */
	public void close(String reason) {
		synchronized (mutex) {
			indexSettingsService.removeListener(applyRefreshSettings);
			if (state != IndexShardState.CLOSED) {
				if (refreshScheduledFuture != null) {
					refreshScheduledFuture.cancel(true);
					refreshScheduledFuture = null;
				}
				if (mergeScheduleFuture != null) {
					mergeScheduleFuture.cancel(true);
					mergeScheduleFuture = null;
				}
			}
			logger.debug("state: [" + state + "]->[" + IndexShardState.CLOSED + "], reason [" + reason + "]");
			state = IndexShardState.CLOSED;
		}
	}

	/**
	 * Check index took.
	 *
	 * @return the long
	 */
	public long checkIndexTook() {
		return this.checkIndexTook;
	}

	/**
	 * Perform recovery prepare for translog.
	 *
	 * @throws RebirthException the rebirth exception
	 */
	public void performRecoveryPrepareForTranslog() throws RebirthException {
		if (state != IndexShardState.RECOVERING) {
			throw new IndexShardNotRecoveringException(shardId, state);
		}

		if (checkIndexOnStartup) {
			checkIndex(true);
		}

		engine.enableGcDeletes(false);
		engine.start();
	}

	/**
	 * Peer recovery status.
	 *
	 * @return the recovery status
	 */
	public RecoveryStatus peerRecoveryStatus() {
		return this.peerRecoveryStatus;
	}

	/**
	 * Perform recovery finalization.
	 *
	 * @param withFlush the with flush
	 * @param peerRecoveryStatus the peer recovery status
	 * @throws RebirthException the rebirth exception
	 */
	public void performRecoveryFinalization(boolean withFlush, RecoveryStatus peerRecoveryStatus)
			throws RebirthException {
		performRecoveryFinalization(withFlush);
		this.peerRecoveryStatus = peerRecoveryStatus;
	}

	/**
	 * Perform recovery finalization.
	 *
	 * @param withFlush the with flush
	 * @throws RebirthException the rebirth exception
	 */
	public void performRecoveryFinalization(boolean withFlush) throws RebirthException {
		if (withFlush) {
			engine.flush(new Engine.Flush());
		}

		translog.clearUnreferenced();
		engine.refresh(new Engine.Refresh(true));
		synchronized (mutex) {
			logger.debug("state: [{}]->[{}], reason [post recovery]", state, IndexShardState.STARTED);
			state = IndexShardState.STARTED;
		}
		startScheduledTasksIfNeeded();
		indicesLifecycle.afterIndexShardStarted(this);
		engine.enableGcDeletes(true);
	}

	/**
	 * Perform recovery operation.
	 *
	 * @param operation the operation
	 * @throws RebirthException the rebirth exception
	 */
	public void performRecoveryOperation(Translog.Operation operation) throws RebirthException {
		if (state != IndexShardState.RECOVERING) {
			throw new IndexShardNotRecoveringException(shardId, state);
		}
		try {
			switch (operation.opType()) {
			case CREATE:
				Translog.Create create = (Translog.Create) operation;
				engine.create(prepareCreate(
						source(create.source().bytes(), create.source().offset(), create.source().length())
								.type(create.type()).id(create.id()).routing(create.routing()).parent(create.parent())
								.timestamp(create.timestamp()).ttl(create.ttl())).version(create.version()).origin(
						Engine.Operation.Origin.RECOVERY));
				break;
			case SAVE:
				Translog.Index index = (Translog.Index) operation;
				engine.index(prepareIndex(
						source(index.source().bytes(), index.source().offset(), index.source().length())
								.type(index.type()).id(index.id()).routing(index.routing()).parent(index.parent())
								.timestamp(index.timestamp()).ttl(index.ttl())).version(index.version()).origin(
						Engine.Operation.Origin.RECOVERY));
				break;
			case DELETE:
				Translog.Delete delete = (Translog.Delete) operation;
				Uid uid = Uid.createUid(delete.uid().text());
				engine.delete(new Engine.Delete(uid.type(), uid.id(), delete.uid()).version(delete.version()).origin(
						Engine.Operation.Origin.RECOVERY));
				break;
			case DELETE_BY_QUERY:
				Translog.DeleteByQuery deleteByQuery = (Translog.DeleteByQuery) operation;
				engine.delete(prepareDeleteByQuery(deleteByQuery.source(), deleteByQuery.filteringAliases(),
						deleteByQuery.types()));
				break;
			default:
				throw new RebirthIllegalStateException("No operation defined for [" + operation + "]");
			}
		} catch (RebirthException e) {
			boolean hasIgnoreOnRecoveryException = false;
			RebirthException current = e;
			while (true) {
				if (current instanceof IgnoreOnRecoveryEngineException) {
					hasIgnoreOnRecoveryException = true;
					break;
				}
				if (current.getCause() instanceof RebirthException) {
					current = (RebirthException) current.getCause();
				} else {
					break;
				}
			}
			if (!hasIgnoreOnRecoveryException) {
				throw e;
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.service.IndexShard#ignoreRecoveryAttempt()
	 */
	public boolean ignoreRecoveryAttempt() {
		IndexShardState state = state();
		return state == IndexShardState.RECOVERING || state == IndexShardState.STARTED
				|| state == IndexShardState.RELOCATED || state == IndexShardState.CLOSED;
	}

	/**
	 * Read allowed.
	 *
	 * @throws IllegalIndexShardStateException the illegal index shard state exception
	 */
	public void readAllowed() throws IllegalIndexShardStateException {
		IndexShardState state = this.state;
		if (state != IndexShardState.STARTED && state != IndexShardState.RELOCATED) {
			throw new IllegalIndexShardStateException(shardId, state,
					"Read operations only allowed when started/relocated");
		}
	}

	/**
	 * Write allowed.
	 *
	 * @throws IllegalIndexShardStateException the illegal index shard state exception
	 */
	private void writeAllowed() throws IllegalIndexShardStateException {
		verifyStartedOrRecovering();
	}

	/**
	 * Verify started or recovering.
	 *
	 * @throws IllegalIndexShardStateException the illegal index shard state exception
	 */
	private void verifyStartedOrRecovering() throws IllegalIndexShardStateException {
		IndexShardState state = this.state;
		if (state != IndexShardState.STARTED && state != IndexShardState.RECOVERING) {
			throw new IllegalIndexShardStateException(shardId, state,
					"write operation only allowed when started/recovering");
		}
	}

	/**
	 * Verify started.
	 *
	 * @throws IllegalIndexShardStateException the illegal index shard state exception
	 */
	private void verifyStarted() throws IllegalIndexShardStateException {
		IndexShardState state = this.state;
		if (state != IndexShardState.STARTED) {
			throw new IndexShardNotStartedException(shardId, state);
		}
	}

	/**
	 * Start scheduled tasks if needed.
	 */
	private void startScheduledTasksIfNeeded() {
		if (refreshInterval.millis() > 0) {
			refreshScheduledFuture = threadPool.schedule(refreshInterval, ThreadPool.Names.SAME, new EngineRefresher());
			logger.debug("scheduling refresher every {}", refreshInterval);
		} else {
			logger.debug("scheduled refresher disabled");
		}

		if (mergeInterval.millis() > 0) {
			mergeScheduleFuture = threadPool.schedule(mergeInterval, ThreadPool.Names.SAME, new EngineMerger());
			logger.debug("scheduling optimizer / merger every {}", mergeInterval);
		} else {
			logger.debug("scheduled optimizer / merger disabled");
		}
	}

	/**
	 * Filter query if needed.
	 *
	 * @param query the query
	 * @param types the types
	 * @return the query
	 */
	private Query filterQueryIfNeeded(Query query, String[] types) {
		Filter searchFilter = mapperService.searchFilter(types);
		if (searchFilter != null) {
			query = new FilteredQuery(query, indexCache.filter().cache(searchFilter));
		}
		return query;
	}

	static {
		IndexMetaData.addDynamicSettings("index.refresh_interval");
	}

	/**
	 * The Class ApplyRefreshSettings.
	 *
	 * @author l.xue.nong
	 */
	private class ApplyRefreshSettings implements IndexSettingsService.Listener {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.settings.IndexSettingsService.Listener#onRefreshSettings(cn.com.rebirth.commons.settings.Settings)
		 */
		@Override
		public void onRefreshSettings(Settings settings) {
			synchronized (mutex) {
				if (state == IndexShardState.CLOSED) {
					return;
				}
				TimeValue refreshInterval = settings.getAsTime("engine.robin.refresh_interval",
						settings.getAsTime("index.refresh_interval", InternalIndexShard.this.refreshInterval));
				if (!refreshInterval.equals(InternalIndexShard.this.refreshInterval)) {
					logger.info("updating refresh_interval from [{}] to [{}]", InternalIndexShard.this.refreshInterval,
							refreshInterval);
					if (refreshScheduledFuture != null) {
						refreshScheduledFuture.cancel(false);
						refreshScheduledFuture = null;
					}
					InternalIndexShard.this.refreshInterval = refreshInterval;
					if (refreshInterval.millis() > 0) {
						refreshScheduledFuture = threadPool.schedule(refreshInterval, ThreadPool.Names.SAME,
								new EngineRefresher());
					}
				}
			}
		}
	}

	/**
	 * The Class EngineRefresher.
	 *
	 * @author l.xue.nong
	 */
	private class EngineRefresher implements Runnable {

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {

			if (!engine().refreshNeeded()) {
				synchronized (mutex) {
					if (state != IndexShardState.CLOSED) {
						refreshScheduledFuture = threadPool.schedule(refreshInterval, ThreadPool.Names.SAME, this);
					}
				}
				return;
			}
			threadPool.executor(ThreadPool.Names.REFRESH).execute(new Runnable() {
				@Override
				public void run() {
					try {
						if (engine.refreshNeeded()) {
							refresh(new Engine.Refresh(false));
						}
					} catch (EngineClosedException e) {

					} catch (RefreshFailedEngineException e) {
						if (e.getCause() instanceof InterruptedException) {

						} else if (e.getCause() instanceof ClosedByInterruptException) {

						} else if (e.getCause() instanceof ThreadInterruptedException) {

						} else {
							if (state != IndexShardState.CLOSED) {
								logger.warn("Failed to perform scheduled engine refresh", e);
							}
						}
					} catch (Exception e) {
						if (state != IndexShardState.CLOSED) {
							logger.warn("Failed to perform scheduled engine refresh", e);
						}
					}
					synchronized (mutex) {
						if (state != IndexShardState.CLOSED) {
							refreshScheduledFuture = threadPool.schedule(refreshInterval, ThreadPool.Names.SAME,
									EngineRefresher.this);
						}
					}
				}
			});
		}
	}

	/**
	 * The Class EngineMerger.
	 *
	 * @author l.xue.nong
	 */
	private class EngineMerger implements Runnable {

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (!engine().possibleMergeNeeded()) {
				synchronized (mutex) {
					if (state != IndexShardState.CLOSED) {
						mergeScheduleFuture = threadPool.schedule(mergeInterval, ThreadPool.Names.SAME, this);
					}
				}
				return;
			}
			threadPool.executor(ThreadPool.Names.MERGE).execute(new Runnable() {
				@Override
				public void run() {
					try {
						engine.maybeMerge();
					} catch (EngineClosedException e) {

					} catch (OptimizeFailedEngineException e) {
						if (e.getCause() instanceof EngineClosedException) {

						} else if (e.getCause() instanceof InterruptedException) {

						} else if (e.getCause() instanceof ClosedByInterruptException) {

						} else if (e.getCause() instanceof ThreadInterruptedException) {

						} else {
							if (state != IndexShardState.CLOSED) {
								logger.warn("Failed to perform scheduled engine optimize/merge", e);
							}
						}
					} catch (Exception e) {
						if (state != IndexShardState.CLOSED) {
							logger.warn("Failed to perform scheduled engine optimize/merge", e);
						}
					}
					synchronized (mutex) {
						if (state != IndexShardState.CLOSED) {
							mergeScheduleFuture = threadPool.schedule(mergeInterval, ThreadPool.Names.SAME,
									EngineMerger.this);
						}
					}
				}
			});
		}
	}

	/**
	 * Check index.
	 *
	 * @param throwException the throw exception
	 * @throws IndexShardException the index shard exception
	 */
	private void checkIndex(boolean throwException) throws IndexShardException {
		try {
			checkIndexTook = 0;
			long time = System.currentTimeMillis();
			if (!IndexReader.indexExists(store.directory())) {
				return;
			}
			CheckIndex checkIndex = new CheckIndex(store.directory());
			FastByteArrayOutputStream os = new FastByteArrayOutputStream();
			PrintStream out = new PrintStream(os);
			checkIndex.setInfoStream(out);
			out.flush();
			CheckIndex.Status status = checkIndex.checkIndex();
			if (!status.clean) {
				if (state == IndexShardState.CLOSED) {

					return;
				}
				logger.warn("check index [failure]\n{}", new String(os.underlyingBytes(), 0, os.size()));
				if (throwException) {
					throw new IndexShardException(shardId, "index check failure");
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("check index [success]\n{}", new String(os.underlyingBytes(), 0, os.size()));
				}
			}
			checkIndexTook = System.currentTimeMillis() - time;
		} catch (Exception e) {
			logger.warn("failed to check index", e);
		}
	}
}