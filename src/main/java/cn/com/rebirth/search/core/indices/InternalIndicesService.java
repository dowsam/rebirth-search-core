/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalIndicesService.java 2012-7-6 14:29:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices;

import static cn.com.rebirth.commons.collect.MapBuilder.newMapBuilder;
import static cn.com.rebirth.search.commons.settings.ImmutableSettings.settingsBuilder;
import static cn.com.rebirth.search.core.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_REPLICAS;
import static cn.com.rebirth.search.core.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_SHARDS;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.concurrent.EsExecutors;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.CreationException;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.Injector;
import cn.com.rebirth.search.commons.inject.Injectors;
import cn.com.rebirth.search.commons.inject.ModulesBuilder;
import cn.com.rebirth.search.commons.io.FileSystemUtils;
import cn.com.rebirth.search.core.env.NodeEnvironment;
import cn.com.rebirth.search.core.gateway.Gateway;
import cn.com.rebirth.search.core.index.CloseableIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.IndexModule;
import cn.com.rebirth.search.core.index.IndexNameModule;
import cn.com.rebirth.search.core.index.IndexServiceManagement;
import cn.com.rebirth.search.core.index.LocalNodeIdModule;
import cn.com.rebirth.search.core.index.aliases.IndexAliasesServiceModule;
import cn.com.rebirth.search.core.index.analysis.AnalysisModule;
import cn.com.rebirth.search.core.index.analysis.AnalysisService;
import cn.com.rebirth.search.core.index.cache.CacheStats;
import cn.com.rebirth.search.core.index.cache.IndexCache;
import cn.com.rebirth.search.core.index.cache.IndexCacheModule;
import cn.com.rebirth.search.core.index.engine.IndexEngine;
import cn.com.rebirth.search.core.index.engine.IndexEngineModule;
import cn.com.rebirth.search.core.index.flush.FlushStats;
import cn.com.rebirth.search.core.index.gateway.IndexGateway;
import cn.com.rebirth.search.core.index.gateway.IndexGatewayModule;
import cn.com.rebirth.search.core.index.get.GetStats;
import cn.com.rebirth.search.core.index.indexing.IndexingStats;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.MapperServiceModule;
import cn.com.rebirth.search.core.index.merge.MergeStats;
import cn.com.rebirth.search.core.index.percolator.PercolatorModule;
import cn.com.rebirth.search.core.index.percolator.PercolatorService;
import cn.com.rebirth.search.core.index.query.IndexQueryParserModule;
import cn.com.rebirth.search.core.index.query.IndexQueryParserService;
import cn.com.rebirth.search.core.index.refresh.RefreshStats;
import cn.com.rebirth.search.core.index.search.stats.SearchStats;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.service.InternalIndexService;
import cn.com.rebirth.search.core.index.settings.IndexSettingsModule;
import cn.com.rebirth.search.core.index.shard.DocsStats;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.similarity.SimilarityModule;
import cn.com.rebirth.search.core.index.store.IndexStoreModule;
import cn.com.rebirth.search.core.index.store.StoreStats;
import cn.com.rebirth.search.core.indices.analysis.IndicesAnalysisService;
import cn.com.rebirth.search.core.indices.recovery.RecoverySettings;
import cn.com.rebirth.search.core.indices.store.IndicesStore;
import cn.com.rebirth.search.core.plugins.IndexPluginsModule;
import cn.com.rebirth.search.core.plugins.PluginsService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;

/**
 * The Class InternalIndicesService.
 *
 * @author l.xue.nong
 */
public class InternalIndicesService extends AbstractLifecycleComponent<IndicesService> implements IndicesService {

	/** The node env. */
	private final NodeEnvironment nodeEnv;

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The indices lifecycle. */
	private final InternalIndicesLifecycle indicesLifecycle;

	/** The indices analysis service. */
	private final IndicesAnalysisService indicesAnalysisService;

	/** The indices store. */
	private final IndicesStore indicesStore;

	/** The injector. */
	private final Injector injector;

	/** The plugins service. */
	private final PluginsService pluginsService;

	/** The indices injectors. */
	private final Map<String, Injector> indicesInjectors = new HashMap<String, Injector>();

	/** The indices. */
	private volatile ImmutableMap<String, IndexService> indices = ImmutableMap.of();

	/** The old shards stats. */
	private final OldShardsStats oldShardsStats = new OldShardsStats();

	/**
	 * Instantiates a new internal indices service.
	 *
	 * @param settings the settings
	 * @param nodeEnv the node env
	 * @param threadPool the thread pool
	 * @param indicesLifecycle the indices lifecycle
	 * @param indicesAnalysisService the indices analysis service
	 * @param indicesStore the indices store
	 * @param injector the injector
	 */
	@Inject
	public InternalIndicesService(Settings settings, NodeEnvironment nodeEnv, ThreadPool threadPool,
			IndicesLifecycle indicesLifecycle, IndicesAnalysisService indicesAnalysisService,
			IndicesStore indicesStore, Injector injector) {
		super(settings);
		this.nodeEnv = nodeEnv;
		this.threadPool = threadPool;
		this.indicesLifecycle = (InternalIndicesLifecycle) indicesLifecycle;
		this.indicesAnalysisService = indicesAnalysisService;
		this.indicesStore = indicesStore;
		this.injector = injector;

		this.pluginsService = injector.getInstance(PluginsService.class);

		this.indicesLifecycle.addListener(oldShardsStats);
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
		ImmutableSet<String> indices = ImmutableSet.copyOf(this.indices.keySet());
		final CountDownLatch latch = new CountDownLatch(indices.size());

		final ExecutorService indicesStopExecutor = Executors.newFixedThreadPool(5,
				EsExecutors.daemonThreadFactory("indices_shutdown"));
		final ExecutorService shardsStopExecutor = Executors.newFixedThreadPool(5,
				EsExecutors.daemonThreadFactory("shards_shutdown"));

		for (final String index : indices) {
			indicesStopExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						deleteIndex(index, false, "shutdown", shardsStopExecutor);
					} catch (Exception e) {
						logger.warn("failed to delete index on stop [" + index + "]", e);
					} finally {
						latch.countDown();
					}
				}
			});
		}
		try {
			latch.await();
		} catch (InterruptedException e) {

		} finally {
			shardsStopExecutor.shutdown();
			indicesStopExecutor.shutdown();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
		injector.getInstance(RecoverySettings.class).close();
		indicesStore.close();
		indicesAnalysisService.close();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.indices.IndicesService#indicesLifecycle()
	 */
	@Override
	public IndicesLifecycle indicesLifecycle() {
		return this.indicesLifecycle;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.indices.IndicesService#stats(boolean)
	 */
	@Override
	public NodeIndicesStats stats(boolean includePrevious) {
		DocsStats docsStats = new DocsStats();
		StoreStats storeStats = new StoreStats();
		IndexingStats indexingStats = new IndexingStats();
		GetStats getStats = new GetStats();
		SearchStats searchStats = new SearchStats();
		CacheStats cacheStats = new CacheStats();
		MergeStats mergeStats = new MergeStats();
		RefreshStats refreshStats = new RefreshStats();
		FlushStats flushStats = new FlushStats();

		if (includePrevious) {
			getStats.add(oldShardsStats.getStats);
			indexingStats.add(oldShardsStats.indexingStats);
			searchStats.add(oldShardsStats.searchStats);
			mergeStats.add(oldShardsStats.mergeStats);
			refreshStats.add(oldShardsStats.refreshStats);
			flushStats.add(oldShardsStats.flushStats);
		}

		for (IndexService indexService : indices.values()) {
			for (IndexShard indexShard : indexService) {
				storeStats.add(indexShard.storeStats());
				docsStats.add(indexShard.docStats());
				getStats.add(indexShard.getStats());
				indexingStats.add(indexShard.indexingStats());
				searchStats.add(indexShard.searchStats());
				mergeStats.add(indexShard.mergeStats());
				refreshStats.add(indexShard.refreshStats());
				flushStats.add(indexShard.flushStats());
			}
			cacheStats.add(indexService.cache().stats());
		}
		return new NodeIndicesStats(storeStats, docsStats, indexingStats, getStats, searchStats, cacheStats,
				mergeStats, refreshStats, flushStats);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.indices.IndicesService#changesAllowed()
	 */
	public boolean changesAllowed() {

		return lifecycle.started();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public UnmodifiableIterator<IndexService> iterator() {
		return indices.values().iterator();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.indices.IndicesService#hasIndex(java.lang.String)
	 */
	public boolean hasIndex(String index) {
		return indices.containsKey(index);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.indices.IndicesService#indices()
	 */
	public Set<String> indices() {
		return newHashSet(indices.keySet());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.indices.IndicesService#indexService(java.lang.String)
	 */
	public IndexService indexService(String index) {
		return indices.get(index);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.indices.IndicesService#indexServiceSafe(java.lang.String)
	 */
	@Override
	public IndexService indexServiceSafe(String index) throws IndexMissingException {
		IndexService indexService = indexService(index);
		if (indexService == null) {
			throw new IndexMissingException(new Index(index));
		}
		return indexService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.indices.IndicesService#createIndex(java.lang.String, cn.com.rebirth.commons.settings.Settings, java.lang.String)
	 */
	public synchronized IndexService createIndex(String sIndexName, Settings settings, String localNodeId)
			throws RebirthException {
		if (!lifecycle.started()) {
			throw new RebirthIllegalStateException("Can't create an index [" + sIndexName + "], node is closed");
		}
		Index index = new Index(sIndexName);
		if (indicesInjectors.containsKey(index.name())) {
			throw new IndexAlreadyExistsException(index);
		}

		indicesLifecycle.beforeIndexCreated(index);

		logger.debug("creating Index [" + sIndexName + "], shards [{}]/[{}]", settings.get(SETTING_NUMBER_OF_SHARDS),
				settings.get(SETTING_NUMBER_OF_REPLICAS));

		Settings indexSettings = settingsBuilder().put(this.settings).put(settings)
				.classLoader(settings.getClassLoader()).build();

		ModulesBuilder modules = new ModulesBuilder();
		modules.add(new IndexNameModule(index));
		modules.add(new LocalNodeIdModule(localNodeId));
		modules.add(new IndexSettingsModule(index, indexSettings));
		modules.add(new IndexPluginsModule(indexSettings, pluginsService));
		modules.add(new IndexStoreModule(indexSettings));
		modules.add(new IndexEngineModule(indexSettings));
		modules.add(new AnalysisModule(indexSettings, indicesAnalysisService));
		modules.add(new SimilarityModule(indexSettings));
		modules.add(new IndexCacheModule(indexSettings));
		modules.add(new IndexQueryParserModule(indexSettings));
		modules.add(new MapperServiceModule());
		modules.add(new IndexAliasesServiceModule());
		modules.add(new IndexGatewayModule(indexSettings, injector.getInstance(Gateway.class)));
		modules.add(new IndexModule(indexSettings));
		modules.add(new PercolatorModule());

		Injector indexInjector;
		try {
			indexInjector = modules.createChildInjector(injector);
		} catch (CreationException e) {
			throw new IndexCreationException(index, Injectors.getFirstErrorFailure(e));
		}

		indicesInjectors.put(index.name(), indexInjector);

		IndexService indexService = indexInjector.getInstance(IndexService.class);

		indicesLifecycle.afterIndexCreated(indexService);

		indices = newMapBuilder(indices).put(index.name(), indexService).immutableMap();

		return indexService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.indices.IndicesService#cleanIndex(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized void cleanIndex(String index, String reason) throws RebirthException {
		deleteIndex(index, false, reason, null);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.indices.IndicesService#deleteIndex(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized void deleteIndex(String index, String reason) throws RebirthException {
		deleteIndex(index, true, reason, null);
	}

	/**
	 * Delete index.
	 *
	 * @param index the index
	 * @param delete the delete
	 * @param reason the reason
	 * @param executor the executor
	 * @throws RebirthException the rebirth exception
	 */
	private void deleteIndex(String index, boolean delete, String reason, @Nullable Executor executor)
			throws RebirthException {
		Injector indexInjector;
		IndexService indexService;
		synchronized (this) {
			indexInjector = indicesInjectors.remove(index);
			if (indexInjector == null) {
				if (!delete) {
					return;
				}
				throw new IndexMissingException(new Index(index));
			}
			if (delete) {
				logger.debug("deleting Index [{}]", index);
			}

			Map<String, IndexService> tmpMap = newHashMap(indices);
			indexService = tmpMap.remove(index);
			indices = ImmutableMap.copyOf(tmpMap);
		}

		indicesLifecycle.beforeIndexClosed(indexService, delete);

		for (Class<? extends CloseableIndexComponent> closeable : pluginsService.indexServices()) {
			indexInjector.getInstance(closeable).close(delete);
		}

		((InternalIndexService) indexService).close(delete, reason, executor);

		indexInjector.getInstance(PercolatorService.class).close();
		indexInjector.getInstance(IndexCache.class).close();
		indexInjector.getInstance(AnalysisService.class).close();
		indexInjector.getInstance(IndexEngine.class).close();
		indexInjector.getInstance(IndexServiceManagement.class).close();

		indexInjector.getInstance(IndexGateway.class).close(delete);
		indexInjector.getInstance(MapperService.class).close();
		indexInjector.getInstance(IndexQueryParserService.class).close();

		Injectors.close(injector);

		indicesLifecycle.afterIndexClosed(indexService.index(), delete);

		if (delete) {
			FileSystemUtils.deleteRecursively(nodeEnv.indexLocations(new Index(index)));
		}
	}

	/**
	 * The Class OldShardsStats.
	 *
	 * @author l.xue.nong
	 */
	static class OldShardsStats extends IndicesLifecycle.Listener {

		/** The search stats. */
		final SearchStats searchStats = new SearchStats();

		/** The get stats. */
		final GetStats getStats = new GetStats();

		/** The indexing stats. */
		final IndexingStats indexingStats = new IndexingStats();

		/** The merge stats. */
		final MergeStats mergeStats = new MergeStats();

		/** The refresh stats. */
		final RefreshStats refreshStats = new RefreshStats();

		/** The flush stats. */
		final FlushStats flushStats = new FlushStats();

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.indices.IndicesLifecycle.Listener#beforeIndexShardClosed(cn.com.rebirth.search.core.index.shard.ShardId, cn.com.rebirth.search.core.index.shard.service.IndexShard, boolean)
		 */
		@Override
		public synchronized void beforeIndexShardClosed(ShardId shardId, @Nullable IndexShard indexShard, boolean delete) {
			if (indexShard != null) {
				getStats.add(indexShard.getStats());
				indexingStats.add(indexShard.indexingStats(), false);
				searchStats.add(indexShard.searchStats(), false);
				mergeStats.add(indexShard.mergeStats());
				refreshStats.add(indexShard.refreshStats());
				flushStats.add(indexShard.flushStats());
			}
		}
	}
}