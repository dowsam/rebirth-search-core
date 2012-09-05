/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SizeParseElement.java 2012-7-6 14:29:15 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.query;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.SearchParseException;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class SizeParseElement.
 *
 * @author l.xue.nong
 */
public class SizeParseElement implements SearchParseElement {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchParseElement#parse(cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		XContentParser.Token token = parser.currentToken();
		if (token.isValue()) {
			int size = parser.intValue();
			if (size < 0) {
				throw new SearchParseException(context, "size is set to [" + size
						+ "] and is expected to be higher or equal to 0");
			}
			context.size(size);
		}
	}
}