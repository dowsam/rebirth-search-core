/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FieldQueryExtension.java 2012-3-29 15:04:17 l.xue.nong$$
 */


package org.apache.lucene.queryParser;

import org.apache.lucene.search.Query;

import cn.com.rebirth.search.core.index.query.QueryParseContext;


/**
 * The Interface FieldQueryExtension.
 *
 * @author l.xue.nong
 */
public interface FieldQueryExtension {

	
	/**
	 * Query.
	 *
	 * @param parseContext the parse context
	 * @param queryText the query text
	 * @return the query
	 */
	Query query(QueryParseContext parseContext, String queryText);
}
