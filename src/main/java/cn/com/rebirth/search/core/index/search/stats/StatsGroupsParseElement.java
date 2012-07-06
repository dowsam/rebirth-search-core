/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StatsGroupsParseElement.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search.stats;

import java.util.ArrayList;
import java.util.List;

import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableList;

/**
 * The Class StatsGroupsParseElement.
 *
 * @author l.xue.nong
 */
public class StatsGroupsParseElement implements SearchParseElement {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchParseElement#parse(cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		XContentParser.Token token = parser.currentToken();
		if (token.isValue()) {
			context.groupStats(ImmutableList.of(parser.text()));
		} else if (token == XContentParser.Token.START_ARRAY) {
			List<String> groupStats = new ArrayList<String>(4);
			while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
				groupStats.add(parser.text());
			}
			context.groupStats(groupStats);
		}
	}
}
