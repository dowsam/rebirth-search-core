/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core WrapperQueryParser.java 2012-7-6 14:29:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;

/**
 * The Class WrapperQueryParser.
 *
 * @author l.xue.nong
 */
public class WrapperQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "wrapper";

	/**
	 * Instantiates a new wrapper query parser.
	 */
	@Inject
	public WrapperQueryParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		XContentParser.Token token = parser.nextToken();
		if (token != XContentParser.Token.FIELD_NAME) {
			throw new QueryParsingException(parseContext.index(), "[wrapper] query malformed");
		}
		String fieldName = parser.currentName();
		if (!fieldName.equals("query")) {
			throw new QueryParsingException(parseContext.index(), "[wrapper] query malformed");
		}
		parser.nextToken();

		byte[] querySource = parser.binaryValue();
		XContentParser qSourceParser = XContentFactory.xContent(querySource).createParser(querySource);
		try {
			final QueryParseContext context = new QueryParseContext(parseContext.index(), parseContext.indexQueryParser);
			context.reset(qSourceParser);
			Query result = context.parseInnerQuery();
			parser.nextToken();
			return result;
		} finally {
			qSourceParser.close();
		}
	}
}
