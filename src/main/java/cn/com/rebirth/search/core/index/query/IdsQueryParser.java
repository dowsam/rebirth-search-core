/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IdsQueryParser.java 2012-7-6 14:29:49 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.index.search.UidFilter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * The Class IdsQueryParser.
 *
 * @author l.xue.nong
 */
public class IdsQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "ids";

	/**
	 * Instantiates a new ids query parser.
	 */
	@Inject
	public IdsQueryParser() {
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

		List<String> ids = new ArrayList<String>();
		Collection<String> types = null;
		String currentFieldName = null;
		float boost = 1.0f;
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
					throw new QueryParsingException(parseContext.index(), "[ids] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("type".equals(currentFieldName) || "_type".equals(currentFieldName)) {
					types = ImmutableList.of(parser.text());
				} else if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else {
					throw new QueryParsingException(parseContext.index(), "[ids] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (ids.size() == 0) {
			throw new QueryParsingException(parseContext.index(), "[ids] query, no ids values provided");
		}

		if (types == null || types.isEmpty()) {
			types = parseContext.queryTypes();
		} else if (types.size() == 1 && Iterables.getFirst(types, null).equals("_all")) {
			types = parseContext.mapperService().types();
		}

		UidFilter filter = new UidFilter(types, ids, parseContext.indexCache().bloomCache());

		ConstantScoreQuery query = new ConstantScoreQuery(filter);
		query.setBoost(boost);
		return query;
	}
}
