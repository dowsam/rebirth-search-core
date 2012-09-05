/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RangeQueryParser.java 2012-7-6 14:29:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;

/**
 * The Class RangeQueryParser.
 *
 * @author l.xue.nong
 */
public class RangeQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "range";

	/**
	 * Instantiates a new range query parser.
	 */
	@Inject
	public RangeQueryParser() {
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
			throw new QueryParsingException(parseContext.index(),
					"[range] query malformed, no field to indicate field name");
		}
		String fieldName = parser.currentName();
		token = parser.nextToken();
		if (token != XContentParser.Token.START_OBJECT) {
			throw new QueryParsingException(parseContext.index(),
					"[range] query malformed, after field missing start object");
		}

		String from = null;
		String to = null;
		boolean includeLower = true;
		boolean includeUpper = true;
		float boost = 1.0f;

		String currentFieldName = null;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else {
				if ("from".equals(currentFieldName)) {
					from = parser.textOrNull();
				} else if ("to".equals(currentFieldName)) {
					to = parser.textOrNull();
				} else if ("include_lower".equals(currentFieldName) || "includeLower".equals(currentFieldName)) {
					includeLower = parser.booleanValue();
				} else if ("include_upper".equals(currentFieldName) || "includeUpper".equals(currentFieldName)) {
					includeUpper = parser.booleanValue();
				} else if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else if ("gt".equals(currentFieldName)) {
					from = parser.textOrNull();
					includeLower = false;
				} else if ("gte".equals(currentFieldName) || "ge".equals(currentFieldName)) {
					from = parser.textOrNull();
					includeLower = true;
				} else if ("lt".equals(currentFieldName)) {
					to = parser.textOrNull();
					includeUpper = false;
				} else if ("lte".equals(currentFieldName) || "le".equals(currentFieldName)) {
					to = parser.textOrNull();
					includeUpper = true;
				} else {
					throw new QueryParsingException(parseContext.index(), "[range] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		token = parser.nextToken();
		if (token != XContentParser.Token.END_OBJECT) {
			throw new QueryParsingException(parseContext.index(),
					"[range] query malformed, does not end with an object");
		}

		Query query = null;
		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		if (smartNameFieldMappers != null) {
			if (smartNameFieldMappers.hasMapper()) {
				query = smartNameFieldMappers.mapper().rangeQuery(from, to, includeLower, includeUpper, parseContext);
			}
		}
		if (query == null) {
			query = new TermRangeQuery(fieldName, from, to, includeLower, includeUpper);
		}
		query.setBoost(boost);
		return QueryParsers.wrapSmartNameQuery(query, smartNameFieldMappers, parseContext);
	}
}