/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermsQueryParser.java 2012-7-6 14:29:09 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import static cn.com.rebirth.search.core.index.query.support.QueryParsers.wrapSmartNameQuery;
import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.Queries;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;

/**
 * The Class TermsQueryParser.
 *
 * @author l.xue.nong
 */
public class TermsQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "terms";

	/**
	 * Instantiates a new terms query parser.
	 */
	@Inject
	public TermsQueryParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, "in" };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		String fieldName = null;
		boolean disableCoord = false;
		float boost = 1.0f;
		int minimumNumberShouldMatch = 1;
		List<String> values = newArrayList();

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_ARRAY) {
				fieldName = currentFieldName;
				while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
					String value = parser.text();
					if (value == null) {
						throw new QueryParsingException(parseContext.index(), "No value specified for terms query");
					}
					values.add(value);
				}
			} else if (token.isValue()) {
				if ("disable_coord".equals(currentFieldName) || "disableCoord".equals(currentFieldName)) {
					disableCoord = parser.booleanValue();
				} else if ("minimum_match".equals(currentFieldName) || "minimumMatch".equals(currentFieldName)) {
					minimumNumberShouldMatch = parser.intValue();
				} else if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				}
			} else {
				throw new QueryParsingException(parseContext.index(), "[terms] query does not support ["
						+ currentFieldName + "]");
			}
		}

		FieldMapper mapper = null;
		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		String[] previousTypes = null;
		if (smartNameFieldMappers != null && smartNameFieldMappers.hasMapper()) {
			mapper = smartNameFieldMappers.mapper();
			if (smartNameFieldMappers.explicitTypeInNameWithDocMapper()) {
				previousTypes = QueryParseContext.setTypesWithPrevious(new String[] { smartNameFieldMappers.docMapper()
						.type() });
			}
		}

		try {
			BooleanQuery query = new BooleanQuery(disableCoord);
			for (String value : values) {
				if (mapper != null) {
					query.add(new BooleanClause(mapper.fieldQuery(value, parseContext), BooleanClause.Occur.SHOULD));
				} else {
					query.add(new TermQuery(new Term(fieldName, value)), BooleanClause.Occur.SHOULD);
				}
			}
			query.setBoost(boost);
			if (minimumNumberShouldMatch != -1) {
				query.setMinimumNumberShouldMatch(minimumNumberShouldMatch);
			}
			return wrapSmartNameQuery(Queries.optimizeQuery(Queries.fixNegativeQueryIfNeeded(query)),
					smartNameFieldMappers, parseContext);
		} finally {
			if (smartNameFieldMappers != null && smartNameFieldMappers.explicitTypeInNameWithDocMapper()) {
				QueryParseContext.setTypes(previousTypes);
			}
		}
	}
}
