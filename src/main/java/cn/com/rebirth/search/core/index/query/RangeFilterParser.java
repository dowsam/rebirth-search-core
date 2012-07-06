/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RangeFilterParser.java 2012-3-29 15:01:23 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.TermRangeFilter;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;


/**
 * The Class RangeFilterParser.
 *
 * @author l.xue.nong
 */
public class RangeFilterParser implements FilterParser {

	
	/** The Constant NAME. */
	public static final String NAME = "range";

	
	/**
	 * Instantiates a new range filter parser.
	 */
	@Inject
	public RangeFilterParser() {
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

		boolean cache = true;
		CacheKeyFilter.Key cacheKey = null;
		String fieldName = null;
		String from = null;
		String to = null;
		boolean includeLower = true;
		boolean includeUpper = true;

		String filterName = null;
		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				fieldName = currentFieldName;
				while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
					if (token == XContentParser.Token.FIELD_NAME) {
						currentFieldName = parser.currentName();
					} else {
						if ("from".equals(currentFieldName)) {
							from = parser.textOrNull();
						} else if ("to".equals(currentFieldName)) {
							to = parser.textOrNull();
						} else if ("include_lower".equals(currentFieldName) || "includeLower".equals(currentFieldName)) {
							includeLower = parser.booleanValue();
						} else if ("include_upper".equals(currentFieldName) || "includeUpper".equals(currentFieldName)) {
							includeUpper = parser.booleanValue();
						} else if ("gt".equals(currentFieldName)) {
							from = parser.textOrNull();
							includeLower = false;
						} else if ("gte".equals(currentFieldName) || "ge".equals(currentFieldName)) {
							from = parser.textOrNull();
							includeLower = true;
						} else if ("lt".equals(currentFieldName)) {
							to = parser.textOrNull();
							includeUpper = false;
						} else if ("lte".equals(currentFieldName) || "le".equals(currentFieldName)) {
							to = parser.textOrNull();
							includeUpper = true;
						} else {
							throw new QueryParsingException(parseContext.index(), "[range] filter does not support ["
									+ currentFieldName + "]");
						}
					}
				}
			} else if (token.isValue()) {
				if ("_name".equals(currentFieldName)) {
					filterName = parser.text();
				} else if ("_cache".equals(currentFieldName)) {
					cache = parser.booleanValue();
				} else if ("_cache_key".equals(currentFieldName) || "_cacheKey".equals(currentFieldName)) {
					cacheKey = new CacheKeyFilter.Key(parser.text());
				} else {
					throw new QueryParsingException(parseContext.index(), "[range] filter does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (fieldName == null) {
			throw new QueryParsingException(parseContext.index(), "No field specified for range filter");
		}

		Filter filter = null;
		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		if (smartNameFieldMappers != null) {
			if (smartNameFieldMappers.hasMapper()) {
				filter = smartNameFieldMappers.mapper().rangeFilter(from, to, includeLower, includeUpper, parseContext);
			}
		}
		if (filter == null) {
			filter = new TermRangeFilter(fieldName, from, to, includeLower, includeUpper);
		}

		if (cache) {
			filter = parseContext.cacheFilter(filter, cacheKey);
		}

		filter = QueryParsers.wrapSmartNameFilter(filter, smartNameFieldMappers, parseContext);
		if (filterName != null) {
			parseContext.addNamedFilter(filterName, filter);
		}
		return filter;
	}
}