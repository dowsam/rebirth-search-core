/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermQueryParser.java 2012-7-6 14:30:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;

/**
 * The Class TermQueryParser.
 *
 * @author l.xue.nong
 */
public class TermQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "term";

	/**
	 * Instantiates a new term query parser.
	 */
	@Inject
	public TermQueryParser() {
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
			throw new QueryParsingException(parseContext.index(), "[term] query malformed, no field");
		}
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
						throw new QueryParsingException(parseContext.index(), "[term] query does not support ["
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

		Query query = null;
		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		if (smartNameFieldMappers != null && smartNameFieldMappers.hasMapper()) {
			if (smartNameFieldMappers.explicitTypeInNameWithDocMapper()) {
				String[] previousTypes = QueryParseContext.setTypesWithPrevious(new String[] { smartNameFieldMappers
						.docMapper().type() });
				try {
					query = smartNameFieldMappers.mapper().fieldQuery(value, parseContext);
				} finally {
					QueryParseContext.setTypes(previousTypes);
				}
			} else {
				query = smartNameFieldMappers.mapper().fieldQuery(value, parseContext);
			}
		}
		if (query == null) {
			query = new TermQuery(new Term(fieldName, value));
		}
		query.setBoost(boost);
		return QueryParsers.wrapSmartNameQuery(query, smartNameFieldMappers, parseContext);
	}
}
