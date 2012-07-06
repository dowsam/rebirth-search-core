/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SpanFirstQueryParser.java 2012-3-29 15:01:13 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanQuery;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;


/**
 * The Class SpanFirstQueryParser.
 *
 * @author l.xue.nong
 */
public class SpanFirstQueryParser implements QueryParser {

	
	/** The Constant NAME. */
	public static final String NAME = "span_first";

	
	/**
	 * Instantiates a new span first query parser.
	 */
	@Inject
	public SpanFirstQueryParser() {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, Strings.toCamelCase(NAME) };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryParser#parse(cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		float boost = 1.0f;

		SpanQuery match = null;
		int end = -1;

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("match".equals(currentFieldName)) {
					Query query = parseContext.parseInnerQuery();
					if (!(query instanceof SpanQuery)) {
						throw new QueryParsingException(parseContext.index(),
								"spanFirst [match] must be of type span query");
					}
					match = (SpanQuery) query;
				} else {
					throw new QueryParsingException(parseContext.index(), "[span_first] query does not support ["
							+ currentFieldName + "]");
				}
			} else {
				if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else if ("end".equals(currentFieldName)) {
					end = parser.intValue();
				} else {
					throw new QueryParsingException(parseContext.index(), "[span_first] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}
		if (match == null) {
			throw new QueryParsingException(parseContext.index(), "spanFirst must have [match] span query clause");
		}
		if (end == -1) {
			throw new QueryParsingException(parseContext.index(), "spanFirst must have [end] set for it");
		}

		SpanFirstQuery query = new SpanFirstQuery(match, end);
		query.setBoost(boost);
		return query;
	}
}