/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryBinaryParseElement.java 2012-7-6 14:30:07 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.query;

import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class QueryBinaryParseElement.
 *
 * @author l.xue.nong
 */
public class QueryBinaryParseElement implements SearchParseElement {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchParseElement#parse(cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		byte[] querySource = parser.binaryValue();
		XContentParser qSourceParser = XContentFactory.xContent(querySource).createParser(querySource);
		try {
			context.parsedQuery(context.queryParserService().parse(qSourceParser));
		} finally {
			qSourceParser.close();
		}
	}
}