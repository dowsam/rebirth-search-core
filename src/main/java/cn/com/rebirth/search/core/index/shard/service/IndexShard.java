/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShard.java 2012-7-6 14:30:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard.service;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.engine.EngineException;
import cn.com.rebirth.search.core.index.flush.FlushStats;
import cn.com.rebirth.search.core.index.get.GetStats;
import cn.com.rebirth.search.core.index.get.ShardGetService;
import cn.com.rebirth.search.core.index.indexing.IndexingStats;
import cn.com.rebirth.search.core.index.indexing.ShardIndexingService;
import cn.com.rebirth.search.core.index.mapper.ParsedDocument;
import cn.com.rebirth.search.core.index.mapper.SourceToParse;
import cn.com.rebirth.search.core.index.merge.MergeStats;
import cn.com.rebirth.search.core.index.refresh.RefreshStats;
import cn.com.rebirth.search.core.index.search.stats.SearchStats;
import cn.com.rebirth.search.core.index.search.stats.ShardSearchService;
import cn.com.rebirth.search.core.index.shard.DocsStats;
import cn.com.rebirth.search.core.index.shard.IndexShardComponent;
import cn.com.rebirth.search.core.index.shard.IndexShardState;
import cn.com.rebirth.search.core.index.store.StoreStats;

/**
 * The Interface IndexShard.
 *
 * @author l.xue.nong
 */
public interface IndexShard extends IndexShardComponent {

	/**
	 * Indexing service.
	 *
	 * @return the shard indexing service
	 */
	ShardIndexingService indexingService();

	/**
	 * Gets the service.
	 *
	 * @return the service
	 */
	ShardGetService getService();

	/**
	 * Search service.
	 *
	 * @return the shard search service
	 */
	ShardSearchService searchService();

	/**
	 * Routing entry.
	 *
	 * @return the shard routing
	 */
	ShardRouting routingEntry();

	/**
	 * Doc stats.
	 *
	 * @return the docs stats
	 */
	DocsStats docStats();

	/**
	 * Store stats.
	 *
	 * @return the store stats
	 */
	StoreStats storeStats();

	/**
	 * Indexing stats.
	 *
	 * @param types the types
	 * @return the indexing stats
	 */
	IndexingStats indexingStats(String... types);

	/**
	 * Search stats.
	 *
	 * @param groups the groups
	 * @return the search stats
	 */
	SearchStats searchStats(String... groups);

	/**
	 * Gets the stats.
	 *
	 * @return the stats
	 */
	GetStats getStats();

	/**
	 * Merge stats.
	 *
	 * @return the merge stats
	 */
	MergeStats mergeStats();

	/**
	 * Refresh stats.
	 *
	 * @return the refresh stats
	 */
	RefreshStats refreshStats();

	/**
	 * Flush stats.
	 *
	 * @return the flush stats
	 */
	FlushStats flushStats();

	/**
	 * State.
	 *
	 * @return the index shard state
	 */
	IndexShardState state();

	/**
	 * Prepare create.
	 *
	 * @param source the source
	 * @return the engine. create
	 * @throws RebirthException the rebirth exception
	 */
	Engine.Create prepareCreate(SourceToParse source) throws RebirthException;

	/**
	 * Creates the.
	 *
	 * @param create the create
	 * @return the parsed document
	 * @throws RebirthException the rebirth exception
	 */
	ParsedDocument create(Engine.Create create) throws RebirthException;

	/**
	 * Prepare index.
	 *
	 * @param source the source
	 * @return the engine. index
	 * @throws RebirthException the rebirth exception
	 */
	Engine.Index prepareIndex(SourceToParse source) throws RebirthException;

	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the parsed document
	 * @throws RebirthException the rebirth exception
	 */
	ParsedDocument index(Engine.Index index) throws RebirthException;

	/**
	 * Prepare delete.
	 *
	 * @param type the type
	 * @param id the id
	 * @param version the version
	 * @return the engine. delete
	 * @throws RebirthException the rebirth exception
	 */
	Engine.Delete prepareDelete(String type, String id, long version) throws RebirthException;

	/**
	 * Delete.
	 *
	 * @param delete the delete
	 * @throws RebirthException the rebirth exception
	 */
	void delete(Engine.Delete delete) throws RebirthException;

	/**
	 * Prepare delete by query.
	 *
	 * @param querySource the query source
	 * @param filteringAliases the filtering aliases
	 * @param types the types
	 * @return the engine. delete by query
	 * @throws RebirthException the rebirth exception
	 */
	Engine.DeleteByQuery prepareDeleteByQuery(BytesHolder querySource, @Nullable String[] filteringAliases,
			String... types) throws RebirthException;

	/**
	 * Delete by query.
	 *
	 * @param deleteByQuery the delete by query
	 * @throws RebirthException the rebirth exception
	 */
	void deleteByQuery(Engine.DeleteByQuery deleteByQuery) throws RebirthException;

	/**
	 * Gets the.
	 *
	 * @param get the get
	 * @return the engine. get result
	 * @throws RebirthException the rebirth exception
	 */
	Engine.GetResult get(Engine.Get get) throws RebirthException;

	/**
	 * Count.
	 *
	 * @param minScore the min score
	 * @param querySource the query source
	 * @param filteringAliases the filtering aliases
	 * @param types the types
	 * @return the long
	 * @throws RebirthException the rebirth exception
	 */
	long count(float minScore, byte[] querySource, @Nullable String[] filteringAliases, String... types)
			throws RebirthException;

	/**
	 * Count.
	 *
	 * @param minScore the min score
	 * @param querySource the query source
	 * @param querySourceOffset the query source offset
	 * @param querySourceLength the query source length
	 * @param filteringAliases the filtering aliases
	 * @param types the types
	 * @return the long
	 * @throws RebirthException the rebirth exception
	 */
	long count(float minScore, byte[] querySource, int querySourceOffset, int querySourceLength,
			@Nullable String[] filteringAliases, String... types) throws RebirthException;

	/**
	 * Refresh.
	 *
	 * @param refresh the refresh
	 * @throws RebirthException the rebirth exception
	 */
	void refresh(Engine.Refresh refresh) throws RebirthException;

	/**
	 * Flush.
	 *
	 * @param flush the flush
	 * @throws RebirthException the rebirth exception
	 */
	void flush(Engine.Flush flush) throws RebirthException;

	/**
	 * Optimize.
	 *
	 * @param optimize the optimize
	 * @throws RebirthException the rebirth exception
	 */
	void optimize(Engine.Optimize optimize) throws RebirthException;

	/**
	 * Snapshot.
	 *
	 * @param <T> the generic type
	 * @param snapshotHandler the snapshot handler
	 * @return the t
	 * @throws EngineException the engine exception
	 */
	<T> T snapshot(Engine.SnapshotHandler<T> snapshotHandler) throws EngineException;

	/**
	 * Recover.
	 *
	 * @param recoveryHandler the recovery handler
	 * @throws EngineException the engine exception
	 */
	void recover(Engine.RecoveryHandler recoveryHandler) throws EngineException;

	/**
	 * Searcher.
	 *
	 * @return the engine. searcher
	 */
	Engine.Searcher searcher();

	/**
	 * Ignore recovery attempt.
	 *
	 * @return true, if successful
	 */
	public boolean ignoreRecoveryAttempt();
}
