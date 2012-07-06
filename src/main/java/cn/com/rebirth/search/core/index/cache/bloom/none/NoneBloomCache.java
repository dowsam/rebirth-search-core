/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NoneBloomCache.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.bloom.none;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.bloom.BloomFilter;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.bloom.BloomCache;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class NoneBloomCache.
 *
 * @author l.xue.nong
 */
public class NoneBloomCache extends AbstractIndexComponent implements BloomCache {

	/**
	 * Instantiates a new none bloom cache.
	 *
	 * @param index the index
	 */
	public NoneBloomCache(Index index) {
		super(index, ImmutableSettings.Builder.EMPTY_SETTINGS);
	}

	/**
	 * Instantiates a new none bloom cache.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 */
	@Inject
	public NoneBloomCache(Index index, @IndexSettings Settings indexSettings) {
		super(index, indexSettings);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.bloom.BloomCache#filter(org.apache.lucene.index.IndexReader, java.lang.String, boolean)
	 */
	@Override
	public BloomFilter filter(IndexReader reader, String fieldName, boolean asyncLoad) {
		return BloomFilter.NONE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.bloom.BloomCache#clear()
	 */
	@Override
	public void clear() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.bloom.BloomCache#clear(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public void clear(IndexReader reader) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.bloom.BloomCache#sizeInBytes()
	 */
	@Override
	public long sizeInBytes() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.bloom.BloomCache#sizeInBytes(java.lang.String)
	 */
	@Override
	public long sizeInBytes(String fieldName) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() throws RebirthException {
	}
}