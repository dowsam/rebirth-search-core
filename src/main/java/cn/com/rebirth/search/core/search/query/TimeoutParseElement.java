/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TimeoutParseElement.java 2012-3-29 15:02:21 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.query;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class TimeoutParseElement.
 *
 * @author l.xue.nong
 */
public class TimeoutParseElement implements SearchParseElement {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.SearchParseElement#parse(cn.com.summall.search.commons.xcontent.XContentParser, cn.com.summall.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		XContentParser.Token token = parser.currentToken();
		if (token == XContentParser.Token.VALUE_NUMBER) {
			context.timeoutInMillis(parser.longValue());
		} else {
			context.timeoutInMillis(TimeValue.parseTimeValue(parser.text(), null).millis());
		}
	}
}
