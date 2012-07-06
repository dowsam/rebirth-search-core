/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DisMaxQueryParser.java 2012-3-29 15:00:48 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;


/**
 * The Class DisMaxQueryParser.
 *
 * @author l.xue.nong
 */
public class DisMaxQueryParser implements QueryParser {

	
	/** The Constant NAME. */
	public static final String NAME = "dis_max";

	
	/**
	 * Instantiates a new dis max query parser.
	 */
	@Inject
	public DisMaxQueryParser() {
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
		float tieBreaker = 0.0f;

		List<Query> queries = newArrayList();

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("queries".equals(currentFieldName)) {
					queries.add(parseContext.parseInnerQuery());
				} else {
					throw new QueryParsingException(parseContext.index(), "[dis_max] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("queries".equals(currentFieldName)) {
					while (token != XContentParser.Token.END_ARRAY) {
						queries.add(parseContext.parseInnerQuery());
						token = parser.nextToken();
					}
				} else {
					throw new QueryParsingException(parseContext.index(), "[dis_max] query does not support ["
							+ currentFieldName + "]");
				}
			} else {
				if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else if ("tie_breaker".equals(currentFieldName) || "tieBreaker".equals(currentFieldName)) {
					tieBreaker = parser.floatValue();
				} else {
					throw new QueryParsingException(parseContext.index(), "[dis_max] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		DisjunctionMaxQuery query = new DisjunctionMaxQuery(queries, tieBreaker);
		query.setBoost(boost);
		return query;
	}
}