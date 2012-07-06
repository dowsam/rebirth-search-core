/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractJvmQueryParserCache.java 2012-7-6 14:30:41 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.query.parser.support;

import java.util.concurrent.ConcurrentMap;

import org.apache.lucene.queryParser.QueryParserSettings;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCache;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class AbstractJvmQueryParserCache.
 *
 * @author l.xue.nong
 */
public class AbstractJvmQueryParserCache extends AbstractIndexComponent implements QueryParserCache {

	/** The cache. */
	final ConcurrentMap<QueryParserSettings, Query> cache;

	/**
	 * Instantiates a new abstract jvm query parser cache.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param cache the cache
	 */
	protected AbstractJvmQueryParserCache(Index index, @IndexSettings Settings indexSettings,
			ConcurrentMap<QueryParserSettings, Query> cache) {
		super(index, indexSettings);
		this.cache = cache;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() throws RebirthException {
		clear();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCache#clear()
	 */
	@Override
	public void clear() {
		cache.clear();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCache#get(org.apache.lucene.queryParser.QueryParserSettings)
	 */
	@Override
	public Query get(QueryParserSettings queryString) {
		return cache.get(queryString);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCache#put(org.apache.lucene.queryParser.QueryParserSettings, org.apache.lucene.search.Query)
	 */
	@Override
	public void put(QueryParserSettings queryString, Query query) {
		cache.put(queryString, query);
	}
}
