/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FromParseElement.java 2012-3-29 15:01:50 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.query;

import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.SearchParseException;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class FromParseElement.
 *
 * @author l.xue.nong
 */
public class FromParseElement implements SearchParseElement {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.SearchParseElement#parse(cn.com.summall.search.commons.xcontent.XContentParser, cn.com.summall.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		XContentParser.Token token = parser.currentToken();
		if (token.isValue()) {
			int from = parser.intValue();
			if (from < 0) {
				throw new SearchParseException(context, "from is set to [" + from
						+ "] and is expected to be higher or equal to 0");
			}
			context.from(from);
		}
	}
}
