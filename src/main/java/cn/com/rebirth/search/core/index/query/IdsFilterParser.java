/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IdsFilterParser.java 2012-7-6 14:29:05 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.index.search.UidFilter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * The Class IdsFilterParser.
 *
 * @author l.xue.nong
 */
public class IdsFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "ids";

	/**
	 * Instantiates a new ids filter parser.
	 */
	@Inject
	public IdsFilterParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		List<String> ids = new ArrayList<String>();
		Collection<String> types = null;
		String filterName = null;
		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("values".equals(currentFieldName)) {
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						String value = parser.textOrNull();
						if (value == null) {
							throw new QueryParsingException(parseContext.index(), "No value specified for term filter");
						}
						ids.add(value);
					}
				} else if ("types".equals(currentFieldName) || "type".equals(currentFieldName)) {
					types = new ArrayList<String>();
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						String value = parser.textOrNull();
						if (value == null) {
							throw new QueryParsingException(parseContext.index(), "No type specified for term filter");
						}
						types.add(value);
					}
				} else {
					throw new QueryParsingException(parseContext.index(), "[ids] filter does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("type".equals(currentFieldName) || "_type".equals(currentFieldName)) {
					types = ImmutableList.of(parser.text());
				} else if ("_name".equals(currentFieldName)) {
					filterName = parser.text();
				} else {
					throw new QueryParsingException(parseContext.index(), "[ids] filter does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (ids.size() == 0) {
			throw new QueryParsingException(parseContext.index(), "[ids] filter, no ids values provided");
		}

		if (types == null || types.isEmpty()) {
			types = parseContext.queryTypes();
		} else if (types.size() == 1 && Iterables.getFirst(types, null).equals("_all")) {
			types = parseContext.mapperService().types();
		}

		UidFilter filter = new UidFilter(types, ids, parseContext.indexCache().bloomCache());
		if (filterName != null) {
			parseContext.addNamedFilter(filterName, filter);
		}
		return filter;
	}
}
