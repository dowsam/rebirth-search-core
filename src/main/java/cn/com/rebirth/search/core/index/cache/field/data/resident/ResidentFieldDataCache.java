/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ResidentFieldDataCache.java 2012-3-29 15:00:58 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.cache.field.data.resident;

import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.cache.CacheBuilderHelper;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.metrics.CounterMetric;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.field.data.support.AbstractConcurrentMapFieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldData;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.settings.IndexSettingsService;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;


/**
 * The Class ResidentFieldDataCache.
 *
 * @author l.xue.nong
 */
public class ResidentFieldDataCache extends AbstractConcurrentMapFieldDataCache implements
		RemovalListener<String, FieldData> {

	
	/** The index settings service. */
	private final IndexSettingsService indexSettingsService;

	
	/** The max size. */
	private volatile int maxSize;

	
	/** The expire. */
	private volatile TimeValue expire;

	
	/** The evictions. */
	private final CounterMetric evictions = new CounterMetric();

	
	/** The apply settings. */
	private final ApplySettings applySettings = new ApplySettings();

	
	/**
	 * Instantiates a new resident field data cache.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param indexSettingsService the index settings service
	 */
	@Inject
	public ResidentFieldDataCache(Index index, @IndexSettings Settings indexSettings,
			IndexSettingsService indexSettingsService) {
		super(index, indexSettings);
		this.indexSettingsService = indexSettingsService;

		this.maxSize = indexSettings.getAsInt("index.cache.field.max_size", componentSettings.getAsInt("max_size", -1));
		this.expire = indexSettings.getAsTime("index.cache.field.expire", componentSettings.getAsTime("expire", null));
		logger.debug("using [resident] field cache with max_size [{}], expire [{}]", maxSize, expire);

		indexSettingsService.addListener(applySettings);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.cache.field.data.support.AbstractConcurrentMapFieldDataCache#close()
	 */
	@Override
	public void close() throws RestartException {
		indexSettingsService.removeListener(applySettings);
		super.close();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.cache.field.data.support.AbstractConcurrentMapFieldDataCache#buildFieldDataMap()
	 */
	@Override
	protected Cache<String, FieldData> buildFieldDataMap() {
		CacheBuilder<String, FieldData> cacheBuilder = CacheBuilder.newBuilder().removalListener(this);
		if (maxSize != -1) {
			cacheBuilder.maximumSize(maxSize);
		}
		if (expire != null) {
			cacheBuilder.expireAfterAccess(expire.nanos(), TimeUnit.NANOSECONDS);
		}
		CacheBuilderHelper.disableStats(cacheBuilder);
		return cacheBuilder.build();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.cache.field.data.FieldDataCache#type()
	 */
	@Override
	public String type() {
		return "resident";
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.cache.field.data.FieldDataCache#evictions()
	 */
	@Override
	public long evictions() {
		return evictions.count();
	}

	
	/* (non-Javadoc)
	 * @see com.google.common.cache.RemovalListener#onRemoval(com.google.common.cache.RemovalNotification)
	 */
	@Override
	public void onRemoval(RemovalNotification<String, FieldData> removalNotification) {
		if (removalNotification.wasEvicted()) {
			evictions.inc();
		}
	}

	static {
		IndexMetaData.addDynamicSettings("index.cache.field.max_size", "index.cache.field.expire");
	}

	
	/**
	 * The Class ApplySettings.
	 *
	 * @author l.xue.nong
	 */
	class ApplySettings implements IndexSettingsService.Listener {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.settings.IndexSettingsService.Listener#onRefreshSettings(cn.com.summall.search.commons.settings.Settings)
		 */
		@Override
		public void onRefreshSettings(Settings settings) {
			int maxSize = settings.getAsInt("index.cache.field.max_size", ResidentFieldDataCache.this.maxSize);
			TimeValue expire = settings.getAsTime("index.cache.field.expire", ResidentFieldDataCache.this.expire);
			boolean changed = false;
			if (maxSize != ResidentFieldDataCache.this.maxSize) {
				logger.info("updating index.cache.field.max_size from [{}] to [{}]",
						ResidentFieldDataCache.this.maxSize, maxSize);
				changed = true;
				ResidentFieldDataCache.this.maxSize = maxSize;
			}
			if (!Objects.equal(expire, ResidentFieldDataCache.this.expire)) {
				logger.info("updating index.cache.field.expire from [{}] to [{}]", ResidentFieldDataCache.this.expire,
						expire);
				changed = true;
				ResidentFieldDataCache.this.expire = expire;
			}
			if (changed) {
				clear();
			}
		}
	}
}
