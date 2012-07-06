/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TrackScoresParseElement.java 2012-3-29 15:00:46 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.sort;

import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class TrackScoresParseElement.
 *
 * @author l.xue.nong
 */
public class TrackScoresParseElement implements SearchParseElement {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.SearchParseElement#parse(cn.com.summall.search.commons.xcontent.XContentParser, cn.com.summall.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		XContentParser.Token token = parser.currentToken();
		if (token.isValue()) {
			context.trackScores(parser.booleanValue());
		}
	}
}