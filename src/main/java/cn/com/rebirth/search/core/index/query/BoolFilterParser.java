/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BoolFilterParser.java 2012-7-6 14:30:10 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilterClause;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.XBooleanFilter;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;

/**
 * The Class BoolFilterParser.
 *
 * @author l.xue.nong
 */
public class BoolFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "bool";

	/**
	 * Instantiates a new bool filter parser.
	 */
	@Inject
	public BoolFilterParser() {
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

		XBooleanFilter boolFilter = new XBooleanFilter();

		boolean cache = false;
		CacheKeyFilter.Key cacheKey = null;

		String filterName = null;
		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("must".equals(currentFieldName)) {
					boolFilter.add(new FilterClause(parseContext.parseInnerFilter(), BooleanClause.Occur.MUST));
				} else if ("must_not".equals(currentFieldName) || "mustNot".equals(currentFieldName)) {
					boolFilter.add(new FilterClause(parseContext.parseInnerFilter(), BooleanClause.Occur.MUST_NOT));
				} else if ("should".equals(currentFieldName)) {
					boolFilter.add(new FilterClause(parseContext.parseInnerFilter(), BooleanClause.Occur.SHOULD));
				} else {
					throw new QueryParsingException(parseContext.index(), "[bool] filter does not support ["
							+ currentFieldName + "]");
				}
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("must".equals(currentFieldName)) {
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						boolFilter.add(new FilterClause(parseContext.parseInnerFilter(), BooleanClause.Occur.MUST));
					}
				} else if ("must_not".equals(currentFieldName) || "mustNot".equals(currentFieldName)) {
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						boolFilter.add(new FilterClause(parseContext.parseInnerFilter(), BooleanClause.Occur.MUST_NOT));
					}
				} else if ("should".equals(currentFieldName)) {
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						boolFilter.add(new FilterClause(parseContext.parseInnerFilter(), BooleanClause.Occur.SHOULD));
					}
				} else {
					throw new QueryParsingException(parseContext.index(), "[bool] filter does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("_cache".equals(currentFieldName)) {
					cache = parser.booleanValue();
				} else if ("_name".equals(currentFieldName)) {
					filterName = parser.text();
				} else if ("_cache_key".equals(currentFieldName) || "_cacheKey".equals(currentFieldName)) {
					cacheKey = new CacheKeyFilter.Key(parser.text());
				} else {
					throw new QueryParsingException(parseContext.index(), "[bool] filter does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		Filter filter = boolFilter;
		if (cache) {
			filter = parseContext.cacheFilter(filter, cacheKey);
		}
		if (filterName != null) {
			parseContext.addNamedFilter(filterName, filter);
		}
		return filter;
	}
}