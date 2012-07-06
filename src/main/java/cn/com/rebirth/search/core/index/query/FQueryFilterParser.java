/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FQueryFilterParser.java 2012-7-6 14:30:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;

/**
 * The Class FQueryFilterParser.
 *
 * @author l.xue.nong
 */
public class FQueryFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "fquery";

	/**
	 * Instantiates a new f query filter parser.
	 */
	@Inject
	public FQueryFilterParser() {
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
		XContentParser parser = parseContext.parser();

		Query query = null;
		boolean cache = false;
		CacheKeyFilter.Key cacheKey = null;

		String filterName = null;
		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("query".equals(currentFieldName)) {
					query = parseContext.parseInnerQuery();
				} else {
					throw new QueryParsingException(parseContext.index(), "[fquery] filter does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("_name".equals(currentFieldName)) {
					filterName = parser.text();
				} else if ("_cache".equals(currentFieldName)) {
					cache = parser.booleanValue();
				} else if ("_cache_key".equals(currentFieldName) || "_cacheKey".equals(currentFieldName)) {
					cacheKey = new CacheKeyFilter.Key(parser.text());
				} else {
					throw new QueryParsingException(parseContext.index(), "[fquery] filter does not support ["
							+ currentFieldName + "]");
				}
			}
		}
		Filter filter = new QueryWrapperFilter(query);
		if (cache) {
			filter = parseContext.cacheFilter(filter, cacheKey);
		}
		if (filterName != null) {
			parseContext.addNamedFilter(filterName, filter);
		}
		return filter;
	}
}