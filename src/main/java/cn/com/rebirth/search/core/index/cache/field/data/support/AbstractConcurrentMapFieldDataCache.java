/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractConcurrentMapFieldDataCache.java 2012-7-6 14:29:13 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.field.data.support;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldData;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

import com.google.common.cache.Cache;

/**
 * The Class AbstractConcurrentMapFieldDataCache.
 *
 * @author l.xue.nong
 */
public abstract class AbstractConcurrentMapFieldDataCache extends AbstractIndexComponent implements FieldDataCache,
		IndexReader.ReaderFinishedListener {

	/** The cache. */
	private final ConcurrentMap<Object, Cache<String, FieldData>> cache;

	/** The creation mutex. */
	private final Object creationMutex = new Object();

	/**
	 * Instantiates a new abstract concurrent map field data cache.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 */
	protected AbstractConcurrentMapFieldDataCache(Index index, @IndexSettings Settings indexSettings) {
		super(index, indexSettings);
		this.cache = ConcurrentCollections.newConcurrentMap();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() throws RebirthException {
		clear();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#clear(java.lang.String)
	 */
	@Override
	public void clear(String fieldName) {
		for (Map.Entry<Object, Cache<String, FieldData>> entry : cache.entrySet()) {
			entry.getValue().invalidate(fieldName);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#clear()
	 */
	@Override
	public void clear() {
		cache.clear();
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.index.IndexReader.ReaderFinishedListener#finished(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public void finished(IndexReader reader) {
		clear(reader);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#clear(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public void clear(IndexReader reader) {
		cache.remove(reader.getCoreCacheKey());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#sizeInBytes()
	 */
	@Override
	public long sizeInBytes() {

		long sizeInBytes = 0;
		for (Cache<String, FieldData> map : cache.values()) {
			for (FieldData fieldData : map.asMap().values()) {
				sizeInBytes += fieldData.sizeInBytes();
			}
		}
		return sizeInBytes;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#sizeInBytes(java.lang.String)
	 */
	@Override
	public long sizeInBytes(String fieldName) {
		long sizeInBytes = 0;
		for (Cache<String, FieldData> map : cache.values()) {
			FieldData fieldData = map.getIfPresent(fieldName);
			if (fieldData != null) {
				sizeInBytes += fieldData.sizeInBytes();
			}
		}
		return sizeInBytes;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#cache(cn.com.rebirth.search.core.index.field.data.FieldDataType, org.apache.lucene.index.IndexReader, java.lang.String)
	 */
	@Override
	public FieldData cache(FieldDataType type, IndexReader reader, String fieldName) throws IOException {
		Cache<String, FieldData> fieldDataCache = cache.get(reader.getCoreCacheKey());
		if (fieldDataCache == null) {
			synchronized (creationMutex) {
				fieldDataCache = cache.get(reader.getCoreCacheKey());
				if (fieldDataCache == null) {
					fieldDataCache = buildFieldDataMap();
					reader.addReaderFinishedListener(this);
					cache.put(reader.getCoreCacheKey(), fieldDataCache);
				}
			}
		}
		FieldData fieldData = fieldDataCache.getIfPresent(fieldName);
		if (fieldData == null) {
			synchronized (fieldDataCache) {
				fieldData = fieldDataCache.getIfPresent(fieldName);
				if (fieldData == null) {
					try {
						fieldData = FieldData.load(type, reader, fieldName);
						fieldDataCache.put(fieldName, fieldData);
					} catch (OutOfMemoryError e) {
						logger.warn("loading field [" + fieldName + "] caused out of memory failure", e);
						final OutOfMemoryError outOfMemoryError = new OutOfMemoryError("loading field [" + fieldName
								+ "] caused out of memory failure");
						outOfMemoryError.initCause(e);
						throw outOfMemoryError;
					}
				}
			}
		}
		return fieldData;
	}

	/**
	 * Builds the field data map.
	 *
	 * @return the cache
	 */
	protected abstract Cache<String, FieldData> buildFieldDataMap();
}
