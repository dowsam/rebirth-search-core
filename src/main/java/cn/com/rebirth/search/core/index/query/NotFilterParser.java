/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NotFilterParser.java 2012-3-29 15:02:26 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Filter;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.NotFilter;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;


/**
 * The Class NotFilterParser.
 *
 * @author l.xue.nong
 */
public class NotFilterParser implements FilterParser {

	
	/** The Constant NAME. */
	public static final String NAME = "not";

	
	/**
	 * Instantiates a new not filter parser.
	 */
	@Inject
	public NotFilterParser() {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.FilterParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.FilterParser#parse(cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		Filter filter = null;
		boolean cache = false;
		CacheKeyFilter.Key cacheKey = null;

		String filterName = null;
		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("filter".equals(currentFieldName)) {
					filter = parseContext.parseInnerFilter();
				} else {
					
					filter = parseContext.parseInnerFilter(currentFieldName);
				}
			} else if (token.isValue()) {
				if ("_cache".equals(currentFieldName)) {
					cache = parser.booleanValue();
				} else if ("_name".equals(currentFieldName)) {
					filterName = parser.text();
				} else if ("_cache_key".equals(currentFieldName) || "_cacheKey".equals(currentFieldName)) {
					cacheKey = new CacheKeyFilter.Key(parser.text());
				} else {
					throw new QueryParsingException(parseContext.index(), "[not] filter does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (filter == null) {
			throw new QueryParsingException(parseContext.index(), "filter is required when using `not` filter");
		}

		Filter notFilter = new NotFilter(filter);
		if (cache) {
			notFilter = parseContext.cacheFilter(notFilter, cacheKey);
		}
		if (filterName != null) {
			parseContext.addNamedFilter(filterName, notFilter);
		}
		return notFilter;
	}
}