/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BoostingQueryParser.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.BoostingQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;

/**
 * The Class BoostingQueryParser.
 *
 * @author l.xue.nong
 */
public class BoostingQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "boosting";

	/**
	 * Instantiates a new boosting query parser.
	 */
	@Inject
	public BoostingQueryParser() {
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

		Query positiveQuery = null;
		Query negativeQuery = null;
		float boost = -1;
		float negativeBoost = -1;

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("positive".equals(currentFieldName)) {
					positiveQuery = parseContext.parseInnerQuery();
				} else if ("negative".equals(currentFieldName)) {
					negativeQuery = parseContext.parseInnerQuery();
				} else {
					throw new QueryParsingException(parseContext.index(), "[boosting] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("negative_boost".equals(currentFieldName) || "negativeBoost".equals(currentFieldName)) {
					negativeBoost = parser.floatValue();
				} else if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else {
					throw new QueryParsingException(parseContext.index(), "[boosting] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (positiveQuery == null) {
			throw new QueryParsingException(parseContext.index(),
					"[boosting] query requires 'positive' query to be set'");
		}
		if (negativeQuery == null) {
			throw new QueryParsingException(parseContext.index(),
					"[boosting] query requires 'negative' query to be set'");
		}
		if (negativeBoost == -1) {
			throw new QueryParsingException(parseContext.index(),
					"[boosting] query requires 'negative_boost' to be set'");
		}

		BoostingQuery boostingQuery = new BoostingQuery(positiveQuery, negativeQuery, negativeBoost);
		if (boost != -1) {
			boostingQuery.setBoost(boost);
		}
		return boostingQuery;
	}
}