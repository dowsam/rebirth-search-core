/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TopChildrenQueryParser.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.search.child.TopChildrenQuery;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class TopChildrenQueryParser.
 *
 * @author l.xue.nong
 */
public class TopChildrenQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "top_children";

	/**
	 * Instantiates a new top children query parser.
	 */
	@Inject
	public TopChildrenQueryParser() {
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

		Query query = null;
		float boost = 1.0f;
		String childType = null;
		String scope = null;
		TopChildrenQuery.ScoreType scoreType = TopChildrenQuery.ScoreType.MAX;
		int factor = 5;
		int incrementalFactor = 2;

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("query".equals(currentFieldName)) {

					String[] origTypes = QueryParseContext.setTypesWithPrevious(childType == null ? null
							: new String[] { childType });
					try {
						query = parseContext.parseInnerQuery();
					} finally {
						QueryParseContext.setTypes(origTypes);
					}
				} else {
					throw new QueryParsingException(parseContext.index(), "[top_children] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("type".equals(currentFieldName)) {
					childType = parser.text();
				} else if ("_scope".equals(currentFieldName)) {
					scope = parser.text();
				} else if ("score".equals(currentFieldName)) {
					scoreType = TopChildrenQuery.ScoreType.fromString(parser.text());
				} else if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else if ("factor".equals(currentFieldName)) {
					factor = parser.intValue();
				} else if ("incremental_factor".equals(currentFieldName)
						|| "incrementalFactor".equals(currentFieldName)) {
					incrementalFactor = parser.intValue();
				} else {
					throw new QueryParsingException(parseContext.index(), "[top_children] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}
		if (query == null) {
			throw new QueryParsingException(parseContext.index(), "[child] requires 'query' field");
		}
		if (childType == null) {
			throw new QueryParsingException(parseContext.index(), "[child] requires 'type' field");
		}

		DocumentMapper childDocMapper = parseContext.mapperService().documentMapper(childType);
		if (childDocMapper == null) {
			throw new QueryParsingException(parseContext.index(), "No mapping for for type [" + childType + "]");
		}
		if (childDocMapper.parentFieldMapper() == null) {
			throw new QueryParsingException(parseContext.index(), "Type [" + childType
					+ "] does not have parent mapping");
		}
		String parentType = childDocMapper.parentFieldMapper().type();

		query.setBoost(boost);

		query = new FilteredQuery(query, parseContext.cacheFilter(childDocMapper.typeFilter(), null));

		SearchContext searchContext = SearchContext.current();
		TopChildrenQuery childQuery = new TopChildrenQuery(query, scope, childType, parentType, scoreType, factor,
				incrementalFactor);
		searchContext.addScopePhase(childQuery);
		return childQuery;
	}
}
