/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MissingFieldQueryExtension.java 2012-3-29 15:04:17 l.xue.nong$$
 */


package org.apache.lucene.queryParser;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeFilter;

import cn.com.rebirth.search.commons.lucene.search.DeletionAwareConstantScoreQuery;
import cn.com.rebirth.search.commons.lucene.search.NotFilter;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.QueryParseContext;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;


/**
 * The Class MissingFieldQueryExtension.
 *
 * @author l.xue.nong
 */
public class MissingFieldQueryExtension implements FieldQueryExtension {

	
	/** The Constant NAME. */
	public static final String NAME = "_missing_";

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.queryParser.FieldQueryExtension#query(cn.com.summall.search.core.index.query.QueryParseContext, java.lang.String)
	 */
	@Override
	public Query query(QueryParseContext parseContext, String queryText) {
		String fieldName = queryText;

		Filter filter = null;
		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		if (smartNameFieldMappers != null) {
			if (smartNameFieldMappers.hasMapper()) {
				filter = smartNameFieldMappers.mapper().rangeFilter(null, null, true, true, parseContext);
			}
		}
		if (filter == null) {
			filter = new TermRangeFilter(fieldName, null, null, true, true);
		}

		
		filter = parseContext.cacheFilter(filter, null);
		filter = new NotFilter(filter);
		
		filter = parseContext.cacheFilter(filter, null);

		filter = QueryParsers.wrapSmartNameFilter(filter, smartNameFieldMappers, parseContext);

		return new DeletionAwareConstantScoreQuery(filter);
	}
}
