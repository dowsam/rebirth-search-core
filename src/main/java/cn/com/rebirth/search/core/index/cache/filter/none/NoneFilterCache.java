/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NoneFilterCache.java 2012-7-6 14:29:03 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.filter.none;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.filter.FilterCache;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class NoneFilterCache.
 *
 * @author l.xue.nong
 */
public class NoneFilterCache extends AbstractIndexComponent implements FilterCache {

	/**
	 * Instantiates a new none filter cache.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 */
	@Inject
	public NoneFilterCache(Index index, @IndexSettings Settings indexSettings) {
		super(index, indexSettings);
		logger.debug("Using no filter cache");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#type()
	 */
	@Override
	public String type() {
		return "none";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() {

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#cache(org.apache.lucene.search.Filter)
	 */
	@Override
	public Filter cache(Filter filterToCache) {
		return filterToCache;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#isCached(org.apache.lucene.search.Filter)
	 */
	@Override
	public boolean isCached(Filter filter) {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#clear()
	 */
	@Override
	public void clear() {

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#clear(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public void clear(IndexReader reader) {

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#entriesStats()
	 */
	@Override
	public EntriesStats entriesStats() {
		return new EntriesStats(0, 0);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#evictions()
	 */
	@Override
	public long evictions() {
		return 0;
	}
}