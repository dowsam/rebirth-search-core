/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SpanNotQueryParser.java 2012-3-29 15:01:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanQuery;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;


/**
 * The Class SpanNotQueryParser.
 *
 * @author l.xue.nong
 */
public class SpanNotQueryParser implements QueryParser {

	
	/** The Constant NAME. */
	public static final String NAME = "span_not";

	
	/**
	 * Instantiates a new span not query parser.
	 */
	@Inject
	public SpanNotQueryParser() {
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

		SpanQuery include = null;
		SpanQuery exclude = null;

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("include".equals(currentFieldName)) {
					Query query = parseContext.parseInnerQuery();
					if (!(query instanceof SpanQuery)) {
						throw new QueryParsingException(parseContext.index(),
								"spanNot [include] must be of type span query");
					}
					include = (SpanQuery) query;
				} else if ("exclude".equals(currentFieldName)) {
					Query query = parseContext.parseInnerQuery();
					if (!(query instanceof SpanQuery)) {
						throw new QueryParsingException(parseContext.index(),
								"spanNot [exclude] must be of type span query");
					}
					exclude = (SpanQuery) query;
				} else {
					throw new QueryParsingException(parseContext.index(), "[span_not] query does not support ["
							+ currentFieldName + "]");
				}
			} else {
				if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else {
					throw new QueryParsingException(parseContext.index(), "[span_not] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}
		if (include == null) {
			throw new QueryParsingException(parseContext.index(), "spanNot must have [include] span query clause");
		}
		if (exclude == null) {
			throw new QueryParsingException(parseContext.index(), "spanNot must have [exclude] span query clause");
		}

		SpanNotQuery query = new SpanNotQuery(include, exclude);
		query.setBoost(boost);
		return query;
	}
}