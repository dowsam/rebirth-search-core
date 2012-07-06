/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PrefixQueryParser.java 2012-3-29 15:02:50 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;


/**
 * The Class PrefixQueryParser.
 *
 * @author l.xue.nong
 */
public class PrefixQueryParser implements QueryParser {

	
	/** The Constant NAME. */
	public static final String NAME = "prefix";

	
	/**
	 * Instantiates a new prefix query parser.
	 */
	@Inject
	public PrefixQueryParser() {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryParser#parse(cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		XContentParser.Token token = parser.nextToken();
		if (token != XContentParser.Token.FIELD_NAME) {
			throw new QueryParsingException(parseContext.index(), "[prefix] query malformed, no field");
		}
		String fieldName = parser.currentName();
		String rewriteMethod = null;

		String value = null;
		float boost = 1.0f;
		token = parser.nextToken();
		if (token == XContentParser.Token.START_OBJECT) {
			String currentFieldName = null;
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else if (token.isValue()) {
					if ("prefix".equals(currentFieldName)) {
						value = parser.text();
					} else if ("value".equals(currentFieldName)) {
						value = parser.text();
					} else if ("boost".equals(currentFieldName)) {
						boost = parser.floatValue();
					} else if ("rewrite".equals(currentFieldName)) {
						rewriteMethod = parser.textOrNull();
					}
				} else {
					throw new QueryParsingException(parseContext.index(), "[prefix] query does not support ["
							+ currentFieldName + "]");
				}
			}
			parser.nextToken();
		} else {
			value = parser.text();
			parser.nextToken();
		}

		if (value == null) {
			throw new QueryParsingException(parseContext.index(), "No value specified for prefix query");
		}

		MultiTermQuery.RewriteMethod method = QueryParsers.parseRewriteMethod(rewriteMethod);

		Query query = null;
		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		if (smartNameFieldMappers != null && smartNameFieldMappers.hasMapper()) {
			if (smartNameFieldMappers.explicitTypeInNameWithDocMapper()) {
				String[] previousTypes = QueryParseContext.setTypesWithPrevious(new String[] { smartNameFieldMappers
						.docMapper().type() });
				try {
					query = smartNameFieldMappers.mapper().prefixQuery(value, method, parseContext);
				} finally {
					QueryParseContext.setTypes(previousTypes);
				}
			} else {
				query = smartNameFieldMappers.mapper().prefixQuery(value, method, parseContext);
			}
		}
		if (query == null) {
			PrefixQuery prefixQuery = new PrefixQuery(new Term(fieldName, value));
			prefixQuery.setRewriteMethod(method);
			query = prefixQuery;
		}
		query.setBoost(boost);
		return QueryParsers.wrapSmartNameQuery(query, smartNameFieldMappers, parseContext);
	}
}