/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core OrFilterParser.java 2012-3-29 15:01:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.search.Filter;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.OrFilter;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;


/**
 * The Class OrFilterParser.
 *
 * @author l.xue.nong
 */
public class OrFilterParser implements FilterParser {

	
	/** The Constant NAME. */
	public static final String NAME = "or";

	
	/**
	 * Instantiates a new or filter parser.
	 */
	@Inject
	public OrFilterParser() {
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

		ArrayList<Filter> filters = newArrayList();

		boolean cache = false;
		CacheKeyFilter.Key cacheKey = null;

		String filterName = null;
		String currentFieldName = null;
		XContentParser.Token token = parser.currentToken();
		if (token == XContentParser.Token.START_ARRAY) {
			while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
				filters.add(parseContext.parseInnerFilter());
			}
		} else {
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else if (token == XContentParser.Token.START_ARRAY) {
					if ("filters".equals(currentFieldName)) {
						while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
							filters.add(parseContext.parseInnerFilter());
						}
					} else {
						while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
							filters.add(parseContext.parseInnerFilter());
						}
					}
				} else if (token.isValue()) {
					if ("_cache".equals(currentFieldName)) {
						cache = parser.booleanValue();
					} else if ("_name".equals(currentFieldName)) {
						filterName = parser.text();
					} else if ("_cache_key".equals(currentFieldName) || "_cacheKey".equals(currentFieldName)) {
						cacheKey = new CacheKeyFilter.Key(parser.text());
					} else {
						throw new QueryParsingException(parseContext.index(), "[or] filter does not support ["
								+ currentFieldName + "]");
					}
				}
			}
		}

		if (filters.isEmpty()) {
			throw new QueryParsingException(parseContext.index(), "[or] filter requires 'filters' to be set on it'");
		}

		
		Filter filter = new OrFilter(filters);
		if (cache) {
			filter = parseContext.cacheFilter(filter, cacheKey);
		}
		if (filterName != null) {
			parseContext.addNamedFilter(filterName, filter);
		}
		return filter;
	}
}