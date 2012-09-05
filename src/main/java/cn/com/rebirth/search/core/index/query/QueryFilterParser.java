/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryFilterParser.java 2012-7-6 14:29:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;

import cn.com.rebirth.core.inject.Inject;

/**
 * The Class QueryFilterParser.
 *
 * @author l.xue.nong
 */
public class QueryFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "query";

	/**
	 * Instantiates a new query filter parser.
	 */
	@Inject
	public QueryFilterParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		Query query = parseContext.parseInnerQuery();
		return new QueryWrapperFilter(query);
	}
}