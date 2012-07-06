/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LimitFilterParser.java 2012-7-6 14:30:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Filter;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.LimitFilter;
import cn.com.rebirth.search.commons.xcontent.XContentParser;

/**
 * The Class LimitFilterParser.
 *
 * @author l.xue.nong
 */
public class LimitFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "limit";

	/**
	 * Instantiates a new limit filter parser.
	 */
	@Inject
	public LimitFilterParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		int limit = -1;
		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token.isValue()) {
				if ("value".equals(currentFieldName)) {
					limit = parser.intValue();
				} else {
					throw new QueryParsingException(parseContext.index(), "[limit] filter does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (limit == -1) {
			throw new QueryParsingException(parseContext.index(), "No value specified for limit filter");
		}

		return new LimitFilter(limit);
	}
}
