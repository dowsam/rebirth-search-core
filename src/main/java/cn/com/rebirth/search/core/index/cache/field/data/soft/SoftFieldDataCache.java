/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SoftFieldDataCache.java 2012-7-6 14:29:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.field.data.soft;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.cache.CacheBuilderHelper;
import cn.com.rebirth.search.commons.metrics.CounterMetric;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.field.data.support.AbstractConcurrentMapFieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldData;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * The Class SoftFieldDataCache.
 *
 * @author l.xue.nong
 */
public class SoftFieldDataCache extends AbstractConcurrentMapFieldDataCache implements
		RemovalListener<String, FieldData> {

	/** The evictions. */
	private final CounterMetric evictions = new CounterMetric();

	/**
	 * Instantiates a new soft field data cache.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 */
	@Inject
	public SoftFieldDataCache(Index index, @IndexSettings Settings indexSettings) {
		super(index, indexSettings);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.support.AbstractConcurrentMapFieldDataCache#buildFieldDataMap()
	 */
	@Override
	protected Cache<String, FieldData> buildFieldDataMap() {
		CacheBuilder<String, FieldData> cacheBuilder = CacheBuilder.newBuilder().softValues().removalListener(this);
		CacheBuilderHelper.disableStats(cacheBuilder);
		return cacheBuilder.build();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#evictions()
	 */
	@Override
	public long evictions() {
		return evictions.count();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#type()
	 */
	@Override
	public String type() {
		return "soft";
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
}
