/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CustomBoostFactorQueryParser.java 2012-3-29 15:02:23 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.function.BoostScoreFunction;
import cn.com.rebirth.search.commons.lucene.search.function.FunctionScoreQuery;
import cn.com.rebirth.search.commons.xcontent.XContentParser;


/**
 * The Class CustomBoostFactorQueryParser.
 *
 * @author l.xue.nong
 */
public class CustomBoostFactorQueryParser implements QueryParser {

	
	/** The Constant NAME. */
	public static final String NAME = "custom_boost_factor";

	
	/**
	 * Instantiates a new custom boost factor query parser.
	 */
	@Inject
	public CustomBoostFactorQueryParser() {
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

		Query query = null;
		float boost = 1.0f;
		float boostFactor = 1.0f;

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("query".equals(currentFieldName)) {
					query = parseContext.parseInnerQuery();
				} else {
					throw new QueryParsingException(parseContext.index(),
							"[custom_boost_factor] query does not support [" + currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("boost_factor".equals(currentFieldName) || "boostFactor".equals(currentFieldName)) {
					boostFactor = parser.floatValue();
				} else if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else {
					throw new QueryParsingException(parseContext.index(),
							"[custom_boost_factor] query does not support [" + currentFieldName + "]");
				}
			}
		}
		if (query == null) {
			throw new QueryParsingException(parseContext.index(), "[constant_factor_query] requires 'query' element");
		}
		FunctionScoreQuery functionScoreQuery = new FunctionScoreQuery(query, new BoostScoreFunction(boostFactor));
		functionScoreQuery.setBoost(boost);
		return functionScoreQuery;
	}
}
