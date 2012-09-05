/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TypeFilterParser.java 2012-7-6 14:30:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.TermFilter;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.internal.TypeFieldMapper;

/**
 * The Class TypeFilterParser.
 *
 * @author l.xue.nong
 */
public class TypeFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "type";

	/**
	 * Instantiates a new type filter parser.
	 */
	@Inject
	public TypeFilterParser() {
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

		XContentParser.Token token = parser.nextToken();
		if (token != XContentParser.Token.FIELD_NAME) {
			throw new QueryParsingException(parseContext.index(),
					"[type] filter should have a value field, and the type name");
		}
		String fieldName = parser.currentName();
		if (!fieldName.equals("value")) {
			throw new QueryParsingException(parseContext.index(),
					"[type] filter should have a value field, and the type name");
		}
		token = parser.nextToken();
		if (token != XContentParser.Token.VALUE_STRING) {
			throw new QueryParsingException(parseContext.index(),
					"[type] filter should have a value field, and the type name");
		}
		String type = parser.text();

		parser.nextToken();

		Filter filter;
		DocumentMapper documentMapper = parseContext.mapperService().documentMapper(type);
		if (documentMapper == null) {
			filter = new TermFilter(TypeFieldMapper.TERM_FACTORY.createTerm(type));
		} else {
			filter = documentMapper.typeFilter();
		}
		return parseContext.cacheFilter(filter, null);
	}
}