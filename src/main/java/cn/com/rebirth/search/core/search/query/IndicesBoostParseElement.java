/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesBoostParseElement.java 2012-7-6 14:28:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.query;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class IndicesBoostParseElement.
 *
 * @author l.xue.nong
 */
public class IndicesBoostParseElement implements SearchParseElement {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchParseElement#parse(cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				String indexName = parser.currentName();
				if (indexName.equals(context.shardTarget().index())) {
					parser.nextToken();

					context.queryBoost(parser.floatValue());
				}
			}
		}
	}
}
