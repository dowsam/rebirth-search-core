/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PrefixFilterParser.java 2012-7-6 14:30:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.PrefixFilter;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;

/**
 * The Class PrefixFilterParser.
 *
 * @author l.xue.nong
 */
public class PrefixFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "prefix";

	/**
	 * Instantiates a new prefix filter parser.
	 */
	@Inject
	public PrefixFilterParser() {
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

		boolean cache = true;
		CacheKeyFilter.Key cacheKey = null;
		String fieldName = null;
		String value = null;

		String filterName = null;
		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token.isValue()) {
				if ("_name".equals(currentFieldName)) {
					filterName = parser.text();
				} else if ("_cache".equals(currentFieldName)) {
					cache = parser.booleanValue();
				} else if ("_cache_key".equals(currentFieldName) || "_cacheKey".equals(currentFieldName)) {
					cacheKey = new CacheKeyFilter.Key(parser.text());
				} else {
					fieldName = currentFieldName;
					value = parser.text();
				}
			}
		}

		if (value == null) {
			throw new QueryParsingException(parseContext.index(), "No value specified for prefix filter");
		}

		Filter filter = null;

		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		if (smartNameFieldMappers != null && smartNameFieldMappers.hasMapper()) {
			if (smartNameFieldMappers.explicitTypeInNameWithDocMapper()) {
				String[] previousTypes = QueryParseContext.setTypesWithPrevious(new String[] { smartNameFieldMappers
						.docMapper().type() });
				try {
					filter = smartNameFieldMappers.mapper().prefixFilter(value, parseContext);
				} finally {
					QueryParseContext.setTypes(previousTypes);
				}
			} else {
				filter = smartNameFieldMappers.mapper().prefixFilter(value, parseContext);
			}
		}
		if (filter == null) {
			filter = new PrefixFilter(new Term(fieldName, value));
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