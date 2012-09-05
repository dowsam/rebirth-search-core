/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesFilterCache.java 2012-7-6 14:30:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.cache.filter;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.monitor.jvm.JvmInfo;
import cn.com.rebirth.search.commons.cache.CacheBuilderHelper;
import cn.com.rebirth.search.commons.lucene.docset.DocSet;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.index.cache.filter.support.FilterCacheValue;
import cn.com.rebirth.search.core.index.cache.filter.weighted.WeightedFilterCache;
import cn.com.rebirth.search.core.node.settings.NodeSettingsService;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;

/**
 * The Class IndicesFilterCache.
 *
 * @author l.xue.nong
 */
public class IndicesFilterCache extends AbstractComponent implements
		RemovalListener<WeightedFilterCache.FilterCacheKey, FilterCacheValue<DocSet>> {

	/** The cache. */
	private Cache<WeightedFilterCache.FilterCacheKey, FilterCacheValue<DocSet>> cache;

	/** The size. */
	private volatile String size;

	/** The size in bytes. */
	private volatile long sizeInBytes;

	/** The expire. */
	private volatile TimeValue expire;

	/** The removal listeners. */
	private volatile Map<String, RemovalListener<WeightedFilterCache.FilterCacheKey, FilterCacheValue<DocSet>>> removalListeners = ImmutableMap
			.of();

	static {
		MetaData.addDynamicSettings("indices.cache.filter.size", "indices.cache.filter.expire");
	}

	/**
	 * The Class ApplySettings.
	 *
	 * @author l.xue.nong
	 */
	class ApplySettings implements NodeSettingsService.Listener {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.node.settings.NodeSettingsService.Listener#onRefreshSettings(cn.com.rebirth.commons.settings.Settings)
		 */
		@Override
		public void onRefreshSettings(Settings settings) {
			boolean replace = false;
			String size = settings.get("indices.cache.filter.size", IndicesFilterCache.this.size);
			if (!size.equals(IndicesFilterCache.this.size)) {
				logger.info("updating [indices.cache.filter.size] from [{}] to [{}]", IndicesFilterCache.this.size,
						size);
				IndicesFilterCache.this.size = size;
				replace = true;
			}
			TimeValue expire = settings.getAsTime("indices.cache.filter.expire", IndicesFilterCache.this.expire);
			if (!Objects.equal(expire, IndicesFilterCache.this.expire)) {
				logger.info("updating [indices.cache.filter.expire] from [{}] to [{}]", IndicesFilterCache.this.expire,
						expire);
				IndicesFilterCache.this.expire = expire;
				replace = true;
			}
			if (replace) {
				Cache<WeightedFilterCache.FilterCacheKey, FilterCacheValue<DocSet>> oldCache = IndicesFilterCache.this.cache;
				computeSizeInBytes();
				buildCache();
				oldCache.invalidateAll();
			}
		}
	}

	/**
	 * Instantiates a new indices filter cache.
	 *
	 * @param settings the settings
	 * @param nodeSettingsService the node settings service
	 */
	@Inject
	public IndicesFilterCache(Settings settings, NodeSettingsService nodeSettingsService) {
		super(settings);
		this.size = componentSettings.get("size", "20%");
		this.expire = componentSettings.getAsTime("expire", null);
		computeSizeInBytes();
		buildCache();
		logger.debug("using [node] filter cache with size [{}], actual_size [{}]", size, new ByteSizeValue(sizeInBytes));

		nodeSettingsService.addListener(new ApplySettings());
	}

	/**
	 * Builds the cache.
	 */
	private void buildCache() {
		CacheBuilder<WeightedFilterCache.FilterCacheKey, FilterCacheValue<DocSet>> cacheBuilder = CacheBuilder
				.newBuilder().removalListener(this).maximumWeight(sizeInBytes)
				.weigher(new WeightedFilterCache.FilterCacheValueWeigher());

		cacheBuilder.concurrencyLevel(8);

		if (expire != null) {
			cacheBuilder.expireAfterAccess(expire.millis(), TimeUnit.MILLISECONDS);
		}

		CacheBuilderHelper.disableStats(cacheBuilder);

		cache = cacheBuilder.build();
	}

	/**
	 * Compute size in bytes.
	 */
	private void computeSizeInBytes() {
		if (size.endsWith("%")) {
			double percent = Double.parseDouble(size.substring(0, size.length() - 1));
			sizeInBytes = (long) ((percent / 100) * JvmInfo.jvmInfo().getMem().getHeapMax().bytes());
		} else {
			sizeInBytes = ByteSizeValue.parseBytesSizeValue(size).bytes();
		}
	}

	/**
	 * Adds the removal listener.
	 *
	 * @param index the index
	 * @param listener the listener
	 */
	public synchronized void addRemovalListener(String index,
			RemovalListener<WeightedFilterCache.FilterCacheKey, FilterCacheValue<DocSet>> listener) {
		removalListeners = MapBuilder.newMapBuilder(removalListeners).put(index, listener).immutableMap();
	}

	/**
	 * Removes the removal listener.
	 *
	 * @param index the index
	 */
	public synchronized void removeRemovalListener(String index) {
		removalListeners = MapBuilder.newMapBuilder(removalListeners).remove(index).immutableMap();
	}

	/**
	 * Close.
	 */
	public void close() {
		cache.invalidateAll();
	}

	/**
	 * Cache.
	 *
	 * @return the cache
	 */
	public Cache<WeightedFilterCache.FilterCacheKey, FilterCacheValue<DocSet>> cache() {
		return this.cache;
	}

	/* (non-Javadoc)
	 * @see com.google.common.cache.RemovalListener#onRemoval(com.google.common.cache.RemovalNotification)
	 */
	@Override
	public void onRemoval(
			RemovalNotification<WeightedFilterCache.FilterCacheKey, FilterCacheValue<DocSet>> removalNotification) {
		WeightedFilterCache.FilterCacheKey key = removalNotification.getKey();
		if (key == null) {
			return;
		}
		RemovalListener<WeightedFilterCache.FilterCacheKey, FilterCacheValue<DocSet>> listener = removalListeners
				.get(key.index());
		if (listener != null) {
			listener.onRemoval(removalNotification);
		}
	}
}