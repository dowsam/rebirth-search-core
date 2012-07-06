/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FilterBinaryParseElement.java 2012-7-6 14:29:13 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.query;

import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class FilterBinaryParseElement.
 *
 * @author l.xue.nong
 */
public class FilterBinaryParseElement implements SearchParseElement {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchParseElement#parse(cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		byte[] filterSource = parser.binaryValue();
		XContentParser fSourceParser = XContentFactory.xContent(filterSource).createParser(filterSource);
		try {
			context.parsedFilter(context.queryParserService().parseInnerFilter(fSourceParser));
		} finally {
			fSourceParser.close();
		}
	}
}