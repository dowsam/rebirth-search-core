/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SpanNearQueryParser.java 2012-7-6 14:29:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;

/**
 * The Class SpanNearQueryParser.
 *
 * @author l.xue.nong
 */
public class SpanNearQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "span_near";

	/**
	 * Instantiates a new span near query parser.
	 */
	@Inject
	public SpanNearQueryParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, Strings.toCamelCase(NAME) };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		float boost = 1.0f;
		int slop = -1;
		boolean inOrder = true;
		boolean collectPayloads = true;

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
									"spanNear [clauses] must be of type span query");
						}
						clauses.add((SpanQuery) query);
					}
				} else {
					throw new QueryParsingException(parseContext.index(), "[span_near] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("in_order".equals(currentFieldName) || "inOrder".equals(currentFieldName)) {
					inOrder = parser.booleanValue();
				} else if ("collect_payloads".equals(currentFieldName) || "collectPayloads".equals(currentFieldName)) {
					collectPayloads = parser.booleanValue();
				} else if ("slop".equals(currentFieldName)) {
					slop = parser.intValue();
				} else if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else {
					throw new QueryParsingException(parseContext.index(), "[span_near] query does not support ["
							+ currentFieldName + "]");
				}
			} else {
				throw new QueryParsingException(parseContext.index(), "[span_near] query does not support ["
						+ currentFieldName + "]");
			}
		}
		if (clauses.isEmpty()) {
			throw new QueryParsingException(parseContext.index(), "span_near must include [clauses]");
		}
		if (slop == -1) {
			throw new QueryParsingException(parseContext.index(), "span_near must include [slop]");
		}

		SpanNearQuery query = new SpanNearQuery(clauses.toArray(new SpanQuery[clauses.size()]), slop, inOrder,
				collectPayloads);
		query.setBoost(boost);
		return query;
	}
}