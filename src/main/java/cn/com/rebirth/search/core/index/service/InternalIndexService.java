/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalIndexService.java 2012-3-29 15:02:42 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.service;

import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.exception.RestartIllegalStateException;
import cn.com.rebirth.commons.exception.RestartInterruptedException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.CreationException;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.Injector;
import cn.com.rebirth.search.commons.inject.Injectors;
import cn.com.rebirth.search.commons.inject.ModulesBuilder;
import cn.com.rebirth.search.commons.io.FileSystemUtils;
import cn.com.rebirth.search.core.env.NodeEnvironment;
import cn.com.rebirth.search.core.gateway.none.NoneGateway;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.CloseableIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.IndexShardAlreadyExistsException;
import cn.com.rebirth.search.core.index.IndexShardMissingException;
import cn.com.rebirth.search.core.index.aliases.IndexAliasesService;
import cn.com.rebirth.search.core.index.analysis.AnalysisService;
import cn.com.rebirth.search.core.index.cache.IndexCache;
import cn.com.rebirth.search.core.index.deletionpolicy.DeletionPolicyModule;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.engine.EngineModule;
import cn.com.rebirth.search.core.index.engine.IndexEngine;
import cn.com.rebirth.search.core.index.gateway.IndexGateway;
import cn.com.rebirth.search.core.index.gateway.IndexShardGatewayModule;
import cn.com.rebirth.search.core.index.gateway.IndexShardGatewayService;
import cn.com.rebirth.search.core.index.get.ShardGetModule;
import cn.com.rebirth.search.core.index.indexing.ShardIndexingModule;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.merge.policy.MergePolicyModule;
import cn.com.rebirth.search.core.index.merge.policy.MergePolicyProvider;
import cn.com.rebirth.search.core.index.merge.scheduler.MergeSchedulerModule;
import cn.com.rebirth.search.core.index.percolator.PercolatorService;
import cn.com.rebirth.search.core.index.query.IndexQueryParserService;
import cn.com.rebirth.search.core.index.search.stats.ShardSearchModule;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.IndexShardCreationException;
import cn.com.rebirth.search.core.index.shard.IndexShardManagement;
import cn.com.rebirth.search.core.index.shard.IndexShardModule;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.shard.service.InternalIndexShard;
import cn.com.rebirth.search.core.index.similarity.SimilarityService;
import cn.com.rebirth.search.core.index.store.IndexStore;
import cn.com.rebirth.search.core.index.store.Store;
import cn.com.rebirth.search.core.index.store.StoreModule;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.index.translog.TranslogModule;
import cn.com.rebirth.search.core.index.translog.TranslogService;
import cn.com.rebirth.search.core.indices.IndicesLifecycle;
import cn.com.rebirth.search.core.indices.InternalIndicesLifecycle;
import cn.com.rebirth.search.core.plugins.PluginsService;
import cn.com.rebirth.search.core.plugins.ShardsPluginsModule;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;


/**
 * The Class InternalIndexService.
 *
 * @author l.xue.nong
 */
public class InternalIndexService extends AbstractIndexComponent implements IndexService {

	
	/** The injector. */
	private final Injector injector;

	
	/** The index settings. */
	private final Settings indexSettings;

	
	/** The node env. */
	private final NodeEnvironment nodeEnv;

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The plugins service. */
	private final PluginsService pluginsService;

	
	/** The indices lifecycle. */
	private final InternalIndicesLifecycle indicesLifecycle;

	
	/** The percolator service. */
	private final PercolatorService percolatorService;

	
	/** The analysis service. */
	private final AnalysisService analysisService;

	
	/** The mapper service. */
	private final MapperService mapperService;

	
	/** The query parser service. */
	private final IndexQueryParserService queryParserService;

	
	/** The similarity service. */
	private final SimilarityService similarityService;

	
	/** The aliases service. */
	private final IndexAliasesService aliasesService;

	
	/** The index cache. */
	private final IndexCache indexCache;

	
	/** The index engine. */
	private final IndexEngine indexEngine;

	
	/** The index gateway. */
	private final IndexGateway indexGateway;

	
	/** The index store. */
	private final IndexStore indexStore;

	
	/** The shards injectors. */
	private volatile ImmutableMap<Integer, Injector> shardsInjectors = ImmutableMap.of();

	
	/** The shards. */
	private volatile ImmutableMap<Integer, IndexShard> shards = ImmutableMap.of();

	
	/** The closed. */
	private volatile boolean closed = false;

	
	/**
	 * Instantiates a new internal index service.
	 *
	 * @param injector the injector
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param nodeEnv the node env
	 * @param threadPool the thread pool
	 * @param percolatorService the percolator service
	 * @param analysisService the analysis service
	 * @param mapperService the mapper service
	 * @param queryParserService the query parser service
	 * @param similarityService the similarity service
	 * @param aliasesService the aliases service
	 * @param indexCache the index cache
	 * @param indexEngine the index engine
	 * @param indexGateway the index gateway
	 * @param indexStore the index store
	 */
	@Inject
	public InternalIndexService(Injector injector, Index index, @IndexSettings Settings indexSettings,
			NodeEnvironment nodeEnv, ThreadPool threadPool, PercolatorService percolatorService,
			AnalysisService analysisService, MapperService mapperService, IndexQueryParserService queryParserService,
			SimilarityService similarityService, IndexAliasesService aliasesService, IndexCache indexCache,
			IndexEngine indexEngine, IndexGateway indexGateway, IndexStore indexStore) {
		super(index, indexSettings);
		this.injector = injector;
		this.nodeEnv = nodeEnv;
		this.threadPool = threadPool;
		this.indexSettings = indexSettings;
		this.percolatorService = percolatorService;
		this.analysisService = analysisService;
		this.mapperService = mapperService;
		this.queryParserService = queryParserService;
		this.similarityService = similarityService;
		this.aliasesService = aliasesService;
		this.indexCache = indexCache;
		this.indexEngine = indexEngine;
		this.indexGateway = indexGateway;
		this.indexStore = indexStore;

		this.pluginsService = injector.getInstance(PluginsService.class);
		this.indicesLifecycle = (InternalIndicesLifecycle) injector.getInstance(IndicesLifecycle.class);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#numberOfShards()
	 */
	@Override
	public int numberOfShards() {
		return shards.size();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public UnmodifiableIterator<IndexShard> iterator() {
		return shards.values().iterator();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#hasShard(int)
	 */
	@Override
	public boolean hasShard(int shardId) {
		return shards.containsKey(shardId);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#shard(int)
	 */
	@Override
	public IndexShard shard(int shardId) {
		return shards.get(shardId);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#shardSafe(int)
	 */
	@Override
	public IndexShard shardSafe(int shardId) throws IndexShardMissingException {
		IndexShard indexShard = shard(shardId);
		if (indexShard == null) {
			throw new IndexShardMissingException(new ShardId(index, shardId));
		}
		return indexShard;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#shardIds()
	 */
	@Override
	public ImmutableSet<Integer> shardIds() {
		return ImmutableSet.copyOf(shards.keySet());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#injector()
	 */
	@Override
	public Injector injector() {
		return injector;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#gateway()
	 */
	@Override
	public IndexGateway gateway() {
		return indexGateway;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#store()
	 */
	@Override
	public IndexStore store() {
		return indexStore;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#cache()
	 */
	@Override
	public IndexCache cache() {
		return indexCache;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#percolateService()
	 */
	@Override
	public PercolatorService percolateService() {
		return this.percolatorService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#analysisService()
	 */
	@Override
	public AnalysisService analysisService() {
		return this.analysisService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#mapperService()
	 */
	@Override
	public MapperService mapperService() {
		return mapperService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#queryParserService()
	 */
	@Override
	public IndexQueryParserService queryParserService() {
		return queryParserService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#similarityService()
	 */
	@Override
	public SimilarityService similarityService() {
		return similarityService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#aliasesService()
	 */
	@Override
	public IndexAliasesService aliasesService() {
		return aliasesService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#engine()
	 */
	@Override
	public IndexEngine engine() {
		return indexEngine;
	}

	
	/**
	 * Close.
	 *
	 * @param delete the delete
	 * @param reason the reason
	 * @param executor the executor
	 */
	public void close(final boolean delete, final String reason, @Nullable Executor executor) {
		synchronized (this) {
			closed = true;
		}
		Set<Integer> shardIds = shardIds();
		final CountDownLatch latch = new CountDownLatch(shardIds.size());
		for (final int shardId : shardIds) {
			executor = executor == null ? threadPool.generic() : executor;
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						deleteShard(shardId, delete, !delete, delete, reason);
					} catch (Exception e) {
						logger.warn("failed to close shard, delete [{}]", e, delete);
					} finally {
						latch.countDown();
					}
				}
			});
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new RestartInterruptedException("interrupted closing index [ " + index().name() + "]", e);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#shardInjector(int)
	 */
	@Override
	public Injector shardInjector(int shardId) throws RestartException {
		return shardsInjectors.get(shardId);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#shardInjectorSafe(int)
	 */
	@Override
	public Injector shardInjectorSafe(int shardId) throws IndexShardMissingException {
		Injector shardInjector = shardInjector(shardId);
		if (shardInjector == null) {
			throw new IndexShardMissingException(new ShardId(index, shardId));
		}
		return shardInjector;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#createShard(int)
	 */
	@Override
	public synchronized IndexShard createShard(int sShardId) throws RestartException {
		if (closed) {
			throw new RestartIllegalStateException("Can't create shard [" + index.name() + "][" + sShardId
					+ "], closed");
		}
		ShardId shardId = new ShardId(index, sShardId);
		if (shardsInjectors.containsKey(shardId.id())) {
			throw new IndexShardAlreadyExistsException(shardId + " already exists");
		}

		indicesLifecycle.beforeIndexShardCreated(shardId);

		logger.debug("creating shard_id [{}]", shardId.id());

		ModulesBuilder modules = new ModulesBuilder();
		modules.add(new ShardsPluginsModule(indexSettings, pluginsService));
		modules.add(new IndexShardModule(indexSettings, shardId));
		modules.add(new ShardIndexingModule());
		modules.add(new ShardSearchModule());
		modules.add(new ShardGetModule());
		modules.add(new StoreModule(indexSettings, injector.getInstance(IndexStore.class)));
		modules.add(new DeletionPolicyModule(indexSettings));
		modules.add(new MergePolicyModule(indexSettings));
		modules.add(new MergeSchedulerModule(indexSettings));
		modules.add(new TranslogModule(indexSettings));
		modules.add(new EngineModule(indexSettings));
		modules.add(new IndexShardGatewayModule(injector.getInstance(IndexGateway.class)));

		Injector shardInjector;
		try {
			shardInjector = modules.createChildInjector(injector);
		} catch (CreationException e) {
			throw new IndexShardCreationException(shardId, Injectors.getFirstErrorFailure(e));
		}

		shardsInjectors = MapBuilder.newMapBuilder(shardsInjectors).put(shardId.id(), shardInjector).immutableMap();

		IndexShard indexShard = shardInjector.getInstance(IndexShard.class);

		indicesLifecycle.afterIndexShardCreated(indexShard);

		shards = MapBuilder.newMapBuilder(shards).put(shardId.id(), indexShard).immutableMap();

		return indexShard;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#cleanShard(int, java.lang.String)
	 */
	@Override
	public synchronized void cleanShard(int shardId, String reason) throws RestartException {
		deleteShard(shardId, true, false, false, reason);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.service.IndexService#removeShard(int, java.lang.String)
	 */
	@Override
	public synchronized void removeShard(int shardId, String reason) throws RestartException {
		deleteShard(shardId, false, false, false, reason);
	}

	
	/**
	 * Delete shard.
	 *
	 * @param shardId the shard id
	 * @param delete the delete
	 * @param snapshotGateway the snapshot gateway
	 * @param deleteGateway the delete gateway
	 * @param reason the reason
	 * @throws SumMallSearchException the sum mall search exception
	 */
	private void deleteShard(int shardId, boolean delete, boolean snapshotGateway, boolean deleteGateway, String reason)
			throws RestartException {
		Injector shardInjector;
		IndexShard indexShard;
		synchronized (this) {
			Map<Integer, Injector> tmpShardInjectors = newHashMap(shardsInjectors);
			shardInjector = tmpShardInjectors.remove(shardId);
			if (shardInjector == null) {
				if (!delete) {
					return;
				}
				throw new IndexShardMissingException(new ShardId(index, shardId));
			}
			shardsInjectors = ImmutableMap.copyOf(tmpShardInjectors);
			if (delete) {
				logger.debug("deleting shard_id [{}]", shardId);
			}

			Map<Integer, IndexShard> tmpShardsMap = newHashMap(shards);
			indexShard = tmpShardsMap.remove(shardId);
			shards = ImmutableMap.copyOf(tmpShardsMap);
		}

		ShardId sId = new ShardId(index, shardId);

		indicesLifecycle.beforeIndexShardClosed(sId, indexShard, delete);

		for (Class<? extends CloseableIndexComponent> closeable : pluginsService.shardServices()) {
			try {
				shardInjector.getInstance(closeable).close(delete);
			} catch (Exception e) {
				logger.debug("failed to clean plugin shard service [{}]", e, closeable);
			}
		}

		try {
			
			shardInjector.getInstance(TranslogService.class).close();
		} catch (Exception e) {
			logger.debug("failed to close translog service", e);
			
		}

		
		if (indexShard != null) {
			shardInjector.getInstance(IndexShardManagement.class).close();
		}

		
		
		if (indexShard != null) {
			try {
				((InternalIndexShard) indexShard).close(reason);
			} catch (Exception e) {
				logger.debug("failed to close index shard", e);
				
			}
		}
		try {
			shardInjector.getInstance(Engine.class).close();
		} catch (Exception e) {
			logger.debug("failed to close engine", e);
			
		}

		try {
			shardInjector.getInstance(MergePolicyProvider.class).close(delete);
		} catch (Exception e) {
			logger.debug("failed to close merge policy provider", e);
			
		}

		try {
			
			if (snapshotGateway) {
				shardInjector.getInstance(IndexShardGatewayService.class).snapshotOnClose();
			}
		} catch (Exception e) {
			logger.debug("failed to snapshot gateway on close", e);
			
		}
		try {
			shardInjector.getInstance(IndexShardGatewayService.class).close(deleteGateway);
		} catch (Exception e) {
			logger.debug("failed to close index shard gateway", e);
			
		}
		try {
			
			shardInjector.getInstance(Translog.class).close(delete);
		} catch (Exception e) {
			logger.debug("failed to close translog", e);
			
		}

		
		indicesLifecycle.afterIndexShardClosed(sId, delete);

		
		Store store = shardInjector.getInstance(Store.class);
		if (delete || indexGateway.type().equals(NoneGateway.TYPE) || !indexStore.persistent()) {
			try {
				store.fullDelete();
			} catch (IOException e) {
				logger.warn("failed to clean store on shard deletion", e);
			}
		}
		
		try {
			store.close();
		} catch (IOException e) {
			logger.warn("failed to close store on shard deletion", e);
		}

		Injectors.close(injector);

		
		if (delete || indexGateway.type().equals(NoneGateway.TYPE)) {
			FileSystemUtils.deleteRecursively(nodeEnv.shardLocations(sId));
		}
	}
}