/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ConstantScoreQueryParser.java 2012-7-6 14:30:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.DeletionAwareConstantScoreQuery;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;

/**
 * The Class ConstantScoreQueryParser.
 *
 * @author l.xue.nong
 */
public class ConstantScoreQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "constant_score";

	/**
	 * Instantiates a new constant score query parser.
	 */
	@Inject
	public ConstantScoreQueryParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, Strings.toCamelCase(NAME) };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		Filter filter = null;
		Query query = null;
		float boost = 1.0f;
		boolean cache = false;
		CacheKeyFilter.Key cacheKey = null;

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("filter".equals(currentFieldName)) {
					filter = parseContext.parseInnerFilter();
				} else if ("query".equals(currentFieldName)) {
					query = parseContext.parseInnerQuery();
				} else {
					throw new QueryParsingException(parseContext.index(), "[constant_score] query does not support ["
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
					throw new QueryParsingException(parseContext.index(), "[constant_score] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}
		if (filter == null && query == null) {
			throw new QueryParsingException(parseContext.index(),
					"[constant_score] requires either 'filter' or 'query' element");
		}

		if (filter != null) {

			if (cache) {
				filter = parseContext.cacheFilter(filter, cacheKey);
			}

			Query query1 = new DeletionAwareConstantScoreQuery(filter);
			query1.setBoost(boost);
			return query1;
		}

		query = new ConstantScoreQuery(query);
		query.setBoost(boost);
		return query;
	}
}