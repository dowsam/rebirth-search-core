/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SpanTermQueryParser.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanTermQuery;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;

/**
 * The Class SpanTermQueryParser.
 *
 * @author l.xue.nong
 */
public class SpanTermQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "span_term";

	/**
	 * Instantiates a new span term query parser.
	 */
	@Inject
	public SpanTermQueryParser() {
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

		XContentParser.Token token = parser.currentToken();
		if (token == XContentParser.Token.START_OBJECT) {
			token = parser.nextToken();
		}
		assert token == XContentParser.Token.FIELD_NAME;
		String fieldName = parser.currentName();

		String value = null;
		float boost = 1.0f;
		token = parser.nextToken();
		if (token == XContentParser.Token.START_OBJECT) {
			String currentFieldName = null;
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else {
					if ("term".equals(currentFieldName)) {
						value = parser.text();
					} else if ("value".equals(currentFieldName)) {
						value = parser.text();
					} else if ("boost".equals(currentFieldName)) {
						boost = parser.floatValue();
					} else {
						throw new QueryParsingException(parseContext.index(), "[span_term] query does not support ["
								+ currentFieldName + "]");
					}
				}
			}
			parser.nextToken();
		} else {
			value = parser.text();

			parser.nextToken();
		}

		if (value == null) {
			throw new QueryParsingException(parseContext.index(), "No value specified for term query");
		}

		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		if (smartNameFieldMappers != null) {
			if (smartNameFieldMappers.hasMapper()) {
				fieldName = smartNameFieldMappers.mapper().names().indexName();
				value = smartNameFieldMappers.mapper().indexedValue(value);
			}
		}

		SpanTermQuery query = new SpanTermQuery(new Term(fieldName, value));
		query.setBoost(boost);
		return QueryParsers.wrapSmartNameQuery(query, smartNameFieldMappers, parseContext);
	}
}