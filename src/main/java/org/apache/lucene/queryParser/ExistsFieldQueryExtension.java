/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ExistsFieldQueryExtension.java 2012-7-6 14:30:39 l.xue.nong$$
 */

package org.apache.lucene.queryParser;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeFilter;

import cn.com.rebirth.search.commons.lucene.search.DeletionAwareConstantScoreQuery;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.QueryParseContext;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;

/**
 * The Class ExistsFieldQueryExtension.
 *
 * @author l.xue.nong
 */
public class ExistsFieldQueryExtension implements FieldQueryExtension {

	/** The Constant NAME. */
	public static final String NAME = "_exists_";

	/* (non-Javadoc)
	 * @see org.apache.lucene.queryParser.FieldQueryExtension#query(cn.com.rebirth.search.core.index.query.QueryParseContext, java.lang.String)
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

		filter = QueryParsers.wrapSmartNameFilter(filter, smartNameFieldMappers, parseContext);

		return new DeletionAwareConstantScoreQuery(filter);
	}
}
