/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SpanOrQueryParser.java 2012-3-29 15:00:59 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;


/**
 * The Class SpanOrQueryParser.
 *
 * @author l.xue.nong
 */
public class SpanOrQueryParser implements QueryParser {

	
	/** The Constant NAME. */
	public static final String NAME = "span_or";

	
	/**
	 * Instantiates a new span or query parser.
	 */
	@Inject
	public SpanOrQueryParser() {
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

		List<SpanQuery> clauses = newArrayList();

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("clauses".equals(currentFieldName)) {
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						Query query = parseContext.parseInnerQuery();
						if (!(query instanceof SpanQuery)) {
							throw new QueryParsingException(parseContext.index(),
									"spanOr [clauses] must be of type span query");
						}
						clauses.add((SpanQuery) query);
					}
				} else {
					throw new QueryParsingException(parseContext.index(), "[span_or] query does not support ["
							+ currentFieldName + "]");
				}
			} else {
				if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else {
					throw new QueryParsingException(parseContext.index(), "[span_or] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}
		if (clauses.isEmpty()) {
			throw new QueryParsingException(parseContext.index(), "spanOr must include [clauses]");
		}

		SpanOrQuery query = new SpanOrQuery(clauses.toArray(new SpanQuery[clauses.size()]));
		query.setBoost(boost);
		return query;
	}
}