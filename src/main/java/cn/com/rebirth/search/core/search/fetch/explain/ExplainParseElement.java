/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ExplainParseElement.java 2012-7-6 14:28:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch.explain;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class ExplainParseElement.
 *
 * @author l.xue.nong
 */
public class ExplainParseElement implements SearchParseElement {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchParseElement#parse(cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		XContentParser.Token token = parser.currentToken();
		if (token.isValue()) {
			context.explain(parser.booleanValue());
		}
	}
}
