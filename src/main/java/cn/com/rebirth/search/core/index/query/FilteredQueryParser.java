/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FilteredQueryParser.java 2012-7-6 14:29:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.DeletionAwareConstantScoreQuery;
import cn.com.rebirth.search.commons.lucene.search.Queries;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;

/**
 * The Class FilteredQueryParser.
 *
 * @author l.xue.nong
 */
public class FilteredQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "filtered";

	/**
	 * Instantiates a new filtered query parser.
	 */
	@Inject
	public FilteredQueryParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		Query query = null;
		Filter filter = null;
		float boost = 1.0f;
		boolean cache = false;
		CacheKeyFilter.Key cacheKey = null;

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("query".equals(currentFieldName)) {
					query = parseContext.parseInnerQuery();
				} else if ("filter".equals(currentFieldName)) {
					filter = parseContext.parseInnerFilter();
				} else {
					throw new QueryParsingException(parseContext.index(), "[filtered] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else if ("_cache".equals(currentFieldName)) {
					cache = parser.booleanValue();
				} else if ("_cache_key".equals(currentFieldName) || "_cacheKey".equals(currentFieldName)) {
					cacheKey = new CacheKeyFilter.Key(parser.text());
				} else {
					throw new QueryParsingException(parseContext.index(), "[filtered] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}
		if (query == null) {
			throw new QueryParsingException(parseContext.index(), "[filtered] requires 'query' element");
		}
		if (filter == null) {
			throw new QueryParsingException(parseContext.index(), "[filtered] requires 'filter' element");
		}

		if (cache) {
			filter = parseContext.cacheFilter(filter, cacheKey);
		}

		if (Queries.isMatchAllQuery(query)) {
			Query q = new DeletionAwareConstantScoreQuery(filter);
			q.setBoost(boost);
			return q;
		}

		FilteredQuery filteredQuery = new FilteredQuery(query, filter);
		filteredQuery.setBoost(boost);
		return filteredQuery;
	}
}