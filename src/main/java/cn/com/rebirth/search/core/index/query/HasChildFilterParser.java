/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HasChildFilterParser.java 2012-7-6 14:29:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.search.child.HasChildFilter;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class HasChildFilterParser.
 *
 * @author l.xue.nong
 */
public class HasChildFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "has_child";

	/**
	 * Instantiates a new checks for child filter parser.
	 */
	@Inject
	public HasChildFilterParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, Strings.toCamelCase(NAME) };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		Query query = null;
		String childType = null;
		String scope = null;

		String filterName = null;
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
					throw new QueryParsingException(parseContext.index(), "[has_child] filter does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("type".equals(currentFieldName)) {
					childType = parser.text();
				} else if ("_scope".equals(currentFieldName)) {
					scope = parser.text();
				} else if ("_name".equals(currentFieldName)) {
					filterName = parser.text();
				} else {
					throw new QueryParsingException(parseContext.index(), "[has_child] filter does not support ["
							+ currentFieldName + "]");
				}
			}
		}
		if (query == null) {
			throw new QueryParsingException(parseContext.index(), "[child] filter requires 'query' field");
		}
		if (childType == null) {
			throw new QueryParsingException(parseContext.index(), "[child] filter requires 'type' field");
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

		query = new FilteredQuery(query, parseContext.cacheFilter(childDocMapper.typeFilter(), null));

		SearchContext searchContext = SearchContext.current();

		HasChildFilter childFilter = new HasChildFilter(query, scope, childType, parentType, searchContext);
		searchContext.addScopePhase(childFilter);

		if (filterName != null) {
			parseContext.addNamedFilter(filterName, childFilter);
		}
		return childFilter;
	}
}
