/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexService.java 2012-7-6 14:30:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.service;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.core.inject.Injector;
import cn.com.rebirth.search.core.index.IndexComponent;
import cn.com.rebirth.search.core.index.IndexShardMissingException;
import cn.com.rebirth.search.core.index.aliases.IndexAliasesService;
import cn.com.rebirth.search.core.index.analysis.AnalysisService;
import cn.com.rebirth.search.core.index.cache.IndexCache;
import cn.com.rebirth.search.core.index.engine.IndexEngine;
import cn.com.rebirth.search.core.index.gateway.IndexGateway;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.percolator.PercolatorService;
import cn.com.rebirth.search.core.index.query.IndexQueryParserService;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.similarity.SimilarityService;
import cn.com.rebirth.search.core.index.store.IndexStore;

import com.google.common.collect.ImmutableSet;

/**
 * The Interface IndexService.
 *
 * @author l.xue.nong
 */
public interface IndexService extends IndexComponent, Iterable<IndexShard> {

	/**
	 * Injector.
	 *
	 * @return the injector
	 */
	Injector injector();

	/**
	 * Gateway.
	 *
	 * @return the index gateway
	 */
	IndexGateway gateway();

	/**
	 * Cache.
	 *
	 * @return the index cache
	 */
	IndexCache cache();

	/**
	 * Percolate service.
	 *
	 * @return the percolator service
	 */
	PercolatorService percolateService();

	/**
	 * Analysis service.
	 *
	 * @return the analysis service
	 */
	AnalysisService analysisService();

	/**
	 * Mapper service.
	 *
	 * @return the mapper service
	 */
	MapperService mapperService();

	/**
	 * Query parser service.
	 *
	 * @return the index query parser service
	 */
	IndexQueryParserService queryParserService();

	/**
	 * Similarity service.
	 *
	 * @return the similarity service
	 */
	SimilarityService similarityService();

	/**
	 * Aliases service.
	 *
	 * @return the index aliases service
	 */
	IndexAliasesService aliasesService();

	/**
	 * Engine.
	 *
	 * @return the index engine
	 */
	IndexEngine engine();

	/**
	 * Store.
	 *
	 * @return the index store
	 */
	IndexStore store();

	/**
	 * Creates the shard.
	 *
	 * @param sShardId the s shard id
	 * @return the index shard
	 * @throws RebirthException the rebirth exception
	 */
	IndexShard createShard(int sShardId) throws RebirthException;

	/**
	 * Clean shard.
	 *
	 * @param shardId the shard id
	 * @param reason the reason
	 * @throws RebirthException the rebirth exception
	 */
	void cleanShard(int shardId, String reason) throws RebirthException;

	/**
	 * Removes the shard.
	 *
	 * @param shardId the shard id
	 * @param reason the reason
	 * @throws RebirthException the rebirth exception
	 */
	void removeShard(int shardId, String reason) throws RebirthException;

	/**
	 * Number of shards.
	 *
	 * @return the int
	 */
	int numberOfShards();

	/**
	 * Shard ids.
	 *
	 * @return the immutable set
	 */
	ImmutableSet<Integer> shardIds();

	/**
	 * Checks for shard.
	 *
	 * @param shardId the shard id
	 * @return true, if successful
	 */
	boolean hasShard(int shardId);

	/**
	 * Shard.
	 *
	 * @param shardId the shard id
	 * @return the index shard
	 */
	IndexShard shard(int shardId);

	/**
	 * Shard safe.
	 *
	 * @param shardId the shard id
	 * @return the index shard
	 * @throws IndexShardMissingException the index shard missing exception
	 */
	IndexShard shardSafe(int shardId) throws IndexShardMissingException;

	/**
	 * Shard injector.
	 *
	 * @param shardId the shard id
	 * @return the injector
	 */
	Injector shardInjector(int shardId);

	/**
	 * Shard injector safe.
	 *
	 * @param shardId the shard id
	 * @return the injector
	 * @throws IndexShardMissingException the index shard missing exception
	 */
	Injector shardInjectorSafe(int shardId) throws IndexShardMissingException;
}
