/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryParserCache.java 2012-7-6 14:30:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.query.parser;

import org.apache.lucene.queryParser.QueryParserSettings;
import org.apache.lucene.search.Query;

import cn.com.rebirth.search.commons.component.CloseableComponent;
import cn.com.rebirth.search.core.index.IndexComponent;

/**
 * The Interface QueryParserCache.
 *
 * @author l.xue.nong
 */
public interface QueryParserCache extends IndexComponent, CloseableComponent {

	/**
	 * Gets the.
	 *
	 * @param queryString the query string
	 * @return the query
	 */
	Query get(QueryParserSettings queryString);

	/**
	 * Put.
	 *
	 * @param queryString the query string
	 * @param query the query
	 */
	void put(QueryParserSettings queryString, Query query);

	/**
	 * Clear.
	 */
	void clear();
}
