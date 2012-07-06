/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FilterParser.java 2012-7-6 14:29:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import org.apache.lucene.search.Filter;

import java.io.IOException;

/**
 * The Interface FilterParser.
 *
 * @author l.xue.nong
 */
public interface FilterParser {

	/**
	 * Names.
	 *
	 * @return the string[]
	 */
	String[] names();

	/**
	 * Parses the.
	 *
	 * @param parseContext the parse context
	 * @return the filter
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws QueryParsingException the query parsing exception
	 */
	Filter parse(QueryParseContext parseContext) throws IOException, QueryParsingException;
}