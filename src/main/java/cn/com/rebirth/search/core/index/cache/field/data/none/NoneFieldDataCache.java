/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NoneFieldDataCache.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.field.data.none;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldData;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class NoneFieldDataCache.
 *
 * @author l.xue.nong
 */
public class NoneFieldDataCache extends AbstractIndexComponent implements FieldDataCache {

	/**
	 * Instantiates a new none field data cache.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 */
	@Inject
	public NoneFieldDataCache(Index index, @IndexSettings Settings indexSettings) {
		super(index, indexSettings);
		logger.debug("Using no field cache");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#cache(cn.com.rebirth.search.core.index.field.data.FieldDataType, org.apache.lucene.index.IndexReader, java.lang.String)
	 */
	@Override
	public FieldData cache(FieldDataType type, IndexReader reader, String fieldName) throws IOException {
		return FieldData.load(type, reader, fieldName);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#type()
	 */
	@Override
	public String type() {
		return "none";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#clear(java.lang.String)
	 */
	@Override
	public void clear(String fieldName) {

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#clear()
	 */
	@Override
	public void clear() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#clear(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public void clear(IndexReader reader) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#sizeInBytes()
	 */
	@Override
	public long sizeInBytes() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#sizeInBytes(java.lang.String)
	 */
	@Override
	public long sizeInBytes(String fieldName) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache#evictions()
	 */
	@Override
	public long evictions() {
		return 0;
	}
}
