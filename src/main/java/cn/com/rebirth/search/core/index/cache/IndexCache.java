/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexCache.java 2012-7-6 14:29:15 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.component.CloseableComponent;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.bloom.BloomCache;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.cache.filter.FilterCache;
import cn.com.rebirth.search.core.index.cache.id.IdCache;
import cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCache;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class IndexCache.
 *
 * @author l.xue.nong
 */
public class IndexCache extends AbstractIndexComponent implements CloseableComponent, ClusterStateListener {

	/** The filter cache. */
	private final FilterCache filterCache;

	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	/** The query parser cache. */
	private final QueryParserCache queryParserCache;

	/** The id cache. */
	private final IdCache idCache;

	/** The bloom cache. */
	private final BloomCache bloomCache;

	/** The refresh interval. */
	private final TimeValue refreshInterval;

	/** The cluster service. */
	private ClusterService clusterService;

	/** The latest cache stats timestamp. */
	private long latestCacheStatsTimestamp = -1;

	/** The latest cache stats. */
	private CacheStats latestCacheStats;

	/**
	 * Instantiates a new index cache.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param filterCache the filter cache
	 * @param fieldDataCache the field data cache
	 * @param queryParserCache the query parser cache
	 * @param idCache the id cache
	 * @param bloomCache the bloom cache
	 */
	@Inject
	public IndexCache(Index index, @IndexSettings Settings indexSettings, FilterCache filterCache,
			FieldDataCache fieldDataCache, QueryParserCache queryParserCache, IdCache idCache, BloomCache bloomCache) {
		super(index, indexSettings);
		this.filterCache = filterCache;
		this.fieldDataCache = fieldDataCache;
		this.queryParserCache = queryParserCache;
		this.idCache = idCache;
		this.bloomCache = bloomCache;

		this.refreshInterval = componentSettings.getAsTime("stats.refresh_interval", TimeValue.timeValueSeconds(1));

		logger.debug("Using stats.refresh_interval [{}]", refreshInterval);
	}

	/**
	 * Sets the cluster service.
	 *
	 * @param clusterService the new cluster service
	 */
	@Inject(optional = true)
	public void setClusterService(@Nullable ClusterService clusterService) {
		this.clusterService = clusterService;
		if (clusterService != null) {
			clusterService.add(this);
		}
	}

	/**
	 * Invalidate cache.
	 */
	public synchronized void invalidateCache() {
		FilterCache.EntriesStats filterEntriesStats = filterCache.entriesStats();
		latestCacheStats = new CacheStats(fieldDataCache.evictions(), filterCache.evictions(),
				fieldDataCache.sizeInBytes(), filterEntriesStats.sizeInBytes, filterEntriesStats.count,
				bloomCache.sizeInBytes());
		latestCacheStatsTimestamp = System.currentTimeMillis();
	}

	/**
	 * Stats.
	 *
	 * @return the cache stats
	 */
	public synchronized CacheStats stats() {
		long timestamp = System.currentTimeMillis();
		if ((timestamp - latestCacheStatsTimestamp) > refreshInterval.millis()) {
			FilterCache.EntriesStats filterEntriesStats = filterCache.entriesStats();
			latestCacheStats = new CacheStats(fieldDataCache.evictions(), filterCache.evictions(),
					fieldDataCache.sizeInBytes(), filterEntriesStats.sizeInBytes, filterEntriesStats.count,
					bloomCache.sizeInBytes());
			latestCacheStatsTimestamp = timestamp;
		}
		return latestCacheStats;
	}

	/**
	 * Filter.
	 *
	 * @return the filter cache
	 */
	public FilterCache filter() {
		return filterCache;
	}

	/**
	 * Field data.
	 *
	 * @return the field data cache
	 */
	public FieldDataCache fieldData() {
		return fieldDataCache;
	}

	/**
	 * Id cache.
	 *
	 * @return the id cache
	 */
	public IdCache idCache() {
		return this.idCache;
	}

	/**
	 * Bloom cache.
	 *
	 * @return the bloom cache
	 */
	public BloomCache bloomCache() {
		return this.bloomCache;
	}

	/**
	 * Query parser cache.
	 *
	 * @return the query parser cache
	 */
	public QueryParserCache queryParserCache() {
		return this.queryParserCache;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() throws RebirthException {
		filterCache.close();
		fieldDataCache.close();
		idCache.close();
		queryParserCache.close();
		bloomCache.close();
		if (clusterService != null) {
			clusterService.remove(this);
		}
	}

	/**
	 * Clear.
	 *
	 * @param reader the reader
	 */
	public void clear(IndexReader reader) {
		filterCache.clear(reader);
		fieldDataCache.clear(reader);
		idCache.clear(reader);
		bloomCache.clear(reader);
	}

	/**
	 * Clear.
	 */
	public void clear() {
		filterCache.clear();
		fieldDataCache.clear();
		idCache.clear();
		queryParserCache.clear();
		bloomCache.clear();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.ClusterStateListener#clusterChanged(cn.com.rebirth.search.core.cluster.ClusterChangedEvent)
	 */
	@Override
	public void clusterChanged(ClusterChangedEvent event) {

		if (event.metaDataChanged()) {
			queryParserCache.clear();
		}
	}
}
