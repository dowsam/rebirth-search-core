/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NoneQueryParserCache.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.query.parser.none;

import org.apache.lucene.queryParser.QueryParserSettings;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCache;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class NoneQueryParserCache.
 *
 * @author l.xue.nong
 */
public class NoneQueryParserCache extends AbstractIndexComponent implements QueryParserCache {

	/**
	 * Instantiates a new none query parser cache.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 */
	@Inject
	public NoneQueryParserCache(Index index, @IndexSettings Settings indexSettings) {
		super(index, indexSettings);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCache#get(org.apache.lucene.queryParser.QueryParserSettings)
	 */
	@Override
	public Query get(QueryParserSettings queryString) {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCache#put(org.apache.lucene.queryParser.QueryParserSettings, org.apache.lucene.search.Query)
	 */
	@Override
	public void put(QueryParserSettings queryString, Query query) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCache#clear()
	 */
	@Override
	public void clear() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() throws RebirthException {
	}
}
