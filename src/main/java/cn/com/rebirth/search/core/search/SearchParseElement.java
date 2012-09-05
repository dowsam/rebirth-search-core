/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchParseElement.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Interface SearchParseElement.
 *
 * @author l.xue.nong
 */
public interface SearchParseElement {

	/**
	 * Parses the.
	 *
	 * @param parser the parser
	 * @param context the context
	 * @throws Exception the exception
	 */
	void parse(XContentParser parser, SearchContext context) throws Exception;
}
