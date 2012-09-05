/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MatchAllQueryParser.java 2012-7-6 14:29:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.Queries;

/**
 * The Class MatchAllQueryParser.
 *
 * @author l.xue.nong
 */
public class MatchAllQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "match_all";

	/**
	 * Instantiates a new match all query parser.
	 */
	@Inject
	public MatchAllQueryParser() {
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
		String normsField = null;
		String currentFieldName = null;

		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token.isValue()) {
				if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else if ("norms_field".equals(currentFieldName) || "normsField".equals(currentFieldName)) {
					normsField = parseContext.indexName(parser.text());
				} else {
					throw new QueryParsingException(parseContext.index(), "[match_all] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (boost == 1.0f && normsField == null) {
			return Queries.MATCH_ALL_QUERY;
		}

		MatchAllDocsQuery query = new MatchAllDocsQuery(normsField);
		query.setBoost(boost);
		return query;
	}
}