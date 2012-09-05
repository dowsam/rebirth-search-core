/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MatchAllFilterParser.java 2012-7-6 14:30:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.Queries;

/**
 * The Class MatchAllFilterParser.
 *
 * @author l.xue.nong
 */
public class MatchAllFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "match_all";

	/**
	 * Instantiates a new match all filter parser.
	 */
	@Inject
	public MatchAllFilterParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, Strings.toCamelCase(NAME) };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
		}

		return Queries.MATCH_ALL_FILTER;
	}
}