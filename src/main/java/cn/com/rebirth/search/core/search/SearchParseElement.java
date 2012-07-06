/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchParseElement.java 2012-3-29 15:01:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import cn.com.rebirth.search.commons.xcontent.XContentParser;
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
