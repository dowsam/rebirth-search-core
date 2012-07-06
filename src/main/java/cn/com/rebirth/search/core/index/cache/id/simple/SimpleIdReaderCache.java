/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SimpleIdReaderCache.java 2012-7-6 14:28:52 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.id.simple;

import cn.com.rebirth.search.commons.BytesWrap;
import cn.com.rebirth.search.core.index.cache.id.IdReaderCache;
import cn.com.rebirth.search.core.index.cache.id.IdReaderTypeCache;

import com.google.common.collect.ImmutableMap;

/**
 * The Class SimpleIdReaderCache.
 *
 * @author l.xue.nong
 */
public class SimpleIdReaderCache implements IdReaderCache {

	/** The reader cache key. */
	private final Object readerCacheKey;

	/** The types. */
	private final ImmutableMap<String, SimpleIdReaderTypeCache> types;

	/**
	 * Instantiates a new simple id reader cache.
	 *
	 * @param readerCacheKey the reader cache key
	 * @param types the types
	 */
	public SimpleIdReaderCache(Object readerCacheKey, ImmutableMap<String, SimpleIdReaderTypeCache> types) {
		this.readerCacheKey = readerCacheKey;
		this.types = types;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.id.IdReaderCache#readerCacheKey()
	 */
	@Override
	public Object readerCacheKey() {
		return this.readerCacheKey;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.id.IdReaderCache#type(java.lang.String)
	 */
	@Override
	public IdReaderTypeCache type(String type) {
		return types.get(type);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.id.IdReaderCache#parentIdByDoc(java.lang.String, int)
	 */
	@Override
	public BytesWrap parentIdByDoc(String type, int docId) {
		SimpleIdReaderTypeCache typeCache = types.get(type);
		if (typeCache != null) {
			return typeCache.parentIdByDoc(docId);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.id.IdReaderCache#docById(java.lang.String, cn.com.rebirth.search.commons.BytesWrap)
	 */
	@Override
	public int docById(String type, BytesWrap id) {
		SimpleIdReaderTypeCache typeCache = types.get(type);
		if (typeCache != null) {
			return typeCache.docById(id);
		}
		return -1;
	}

	/**
	 * Can reuse.
	 *
	 * @param id the id
	 * @return the bytes wrap
	 */
	public BytesWrap canReuse(BytesWrap id) {
		for (SimpleIdReaderTypeCache typeCache : types.values()) {
			BytesWrap wrap = typeCache.canReuse(id);
			if (wrap != null) {
				return wrap;
			}
		}
		return null;
	}
}
