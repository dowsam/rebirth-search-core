/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TermFilterParser.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.TermFilter;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;


/**
 * The Class TermFilterParser.
 *
 * @author l.xue.nong
 */
public class TermFilterParser implements FilterParser {

	
	/** The Constant NAME. */
	public static final String NAME = "term";

	
	/**
	 * Instantiates a new term filter parser.
	 */
	@Inject
	public TermFilterParser() {
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

		if (fieldName == null) {
			throw new QueryParsingException(parseContext.index(), "No field specified for term filter");
		}

		if (value == null) {
			throw new QueryParsingException(parseContext.index(), "No value specified for term filter");
		}

		Filter filter = null;
		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		if (smartNameFieldMappers != null && smartNameFieldMappers.hasMapper()) {
			if (smartNameFieldMappers.explicitTypeInNameWithDocMapper()) {
				String[] previousTypes = QueryParseContext.setTypesWithPrevious(new String[] { smartNameFieldMappers
						.docMapper().type() });
				try {
					filter = smartNameFieldMappers.mapper().fieldFilter(value, parseContext);
				} finally {
					QueryParseContext.setTypes(previousTypes);
				}
			} else {
				filter = smartNameFieldMappers.mapper().fieldFilter(value, parseContext);
			}
		}
		if (filter == null) {
			filter = new TermFilter(new Term(fieldName, value));
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