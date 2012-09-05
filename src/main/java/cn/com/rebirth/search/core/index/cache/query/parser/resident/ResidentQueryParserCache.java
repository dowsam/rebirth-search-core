/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ResidentQueryParserCache.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.query.parser.resident;

import java.util.concurrent.TimeUnit;

import org.apache.lucene.queryParser.QueryParserSettings;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.cache.CacheBuilderHelper;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCache;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * The Class ResidentQueryParserCache.
 *
 * @author l.xue.nong
 */
public class ResidentQueryParserCache extends AbstractIndexComponent implements QueryParserCache {

	/** The cache. */
	private final Cache<QueryParserSettings, Query> cache;

	/** The max size. */
	private volatile int maxSize;

	/** The expire. */
	private volatile TimeValue expire;

	/**
	 * Instantiates a new resident query parser cache.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 */
	@Inject
	public ResidentQueryParserCache(Index index, @IndexSettings Settings indexSettings) {
		super(index, indexSettings);

		this.maxSize = indexSettings
				.getAsInt("index.cache.field.max_size", componentSettings.getAsInt("max_size", 100));
		this.expire = indexSettings.getAsTime("index.cache.field.expire", componentSettings.getAsTime("expire", null));
		logger.debug("using [resident] query cache with max_size [{}], expire [{}]", maxSize, expire);

		CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(maxSize);
		if (expire != null) {
			cacheBuilder.expireAfterAccess(expire.nanos(), TimeUnit.NANOSECONDS);
		}

		CacheBuilderHelper.disableStats(cacheBuilder);

		this.cache = cacheBuilder.build();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCache#get(org.apache.lucene.queryParser.QueryParserSettings)
	 */
	@Override
	public Query get(QueryParserSettings queryString) {
		return cache.getIfPresent(queryString);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCache#put(org.apache.lucene.queryParser.QueryParserSettings, org.apache.lucene.search.Query)
	 */
	@Override
	public void put(QueryParserSettings queryString, Query query) {
		cache.put(queryString, query);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCache#clear()
	 */
	@Override
	public void clear() {
		cache.invalidateAll();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() throws RebirthException {
		cache.invalidateAll();
	}
}
