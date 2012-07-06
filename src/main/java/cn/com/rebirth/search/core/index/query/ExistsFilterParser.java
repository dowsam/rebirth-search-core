/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ExistsFilterParser.java 2012-3-29 15:00:59 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.TermRangeFilter;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;


/**
 * The Class ExistsFilterParser.
 *
 * @author l.xue.nong
 */
public class ExistsFilterParser implements FilterParser {

	
	/** The Constant NAME. */
	public static final String NAME = "exists";

	
	/**
	 * Instantiates a new exists filter parser.
	 */
	@Inject
	public ExistsFilterParser() {
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

		String fieldName = null;
		String filterName = null;

		XContentParser.Token token;
		String currentFieldName = null;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token.isValue()) {
				if ("field".equals(currentFieldName)) {
					fieldName = parser.text();
				} else if ("_name".equals(currentFieldName)) {
					filterName = parser.text();
				} else {
					throw new QueryParsingException(parseContext.index(), "[exists] filter does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (fieldName == null) {
			throw new QueryParsingException(parseContext.index(), "exists must be provided with a [field]");
		}

		Filter filter = null;
		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		if (smartNameFieldMappers != null && smartNameFieldMappers.hasMapper()) {
			filter = smartNameFieldMappers.mapper().rangeFilter(null, null, true, true, parseContext);
		}
		if (filter == null) {
			filter = new TermRangeFilter(fieldName, null, null, true, true);
		}

		
		filter = parseContext.cacheFilter(filter, null);

		filter = QueryParsers.wrapSmartNameFilter(filter, smartNameFieldMappers, parseContext);
		if (filterName != null) {
			parseContext.addNamedFilter(filterName, filter);
		}
		return filter;
	}
}
