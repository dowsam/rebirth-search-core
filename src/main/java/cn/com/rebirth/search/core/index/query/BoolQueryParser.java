/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BoolQueryParser.java 2012-7-6 14:28:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.Queries;
import cn.com.rebirth.search.commons.xcontent.XContentParser;

/**
 * The Class BoolQueryParser.
 *
 * @author l.xue.nong
 */
public class BoolQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "bool";

	/**
	 * Instantiates a new bool query parser.
	 *
	 * @param settings the settings
	 */
	@Inject
	public BoolQueryParser(Settings settings) {
		BooleanQuery.setMaxClauseCount(settings.getAsInt("index.query.bool.max_clause_count",
				settings.getAsInt("indices.query.bool.max_clause_count", BooleanQuery.getMaxClauseCount())));
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

		boolean disableCoord = false;
		float boost = 1.0f;
		int minimumNumberShouldMatch = -1;

		List<BooleanClause> clauses = newArrayList();

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("must".equals(currentFieldName)) {
					clauses.add(new BooleanClause(parseContext.parseInnerQuery(), BooleanClause.Occur.MUST));
				} else if ("must_not".equals(currentFieldName) || "mustNot".equals(currentFieldName)) {
					clauses.add(new BooleanClause(parseContext.parseInnerQuery(), BooleanClause.Occur.MUST_NOT));
				} else if ("should".equals(currentFieldName)) {
					clauses.add(new BooleanClause(parseContext.parseInnerQuery(), BooleanClause.Occur.SHOULD));
				} else {
					throw new QueryParsingException(parseContext.index(), "[bool] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("must".equals(currentFieldName)) {
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						clauses.add(new BooleanClause(parseContext.parseInnerQuery(), BooleanClause.Occur.MUST));
					}
				} else if ("must_not".equals(currentFieldName) || "mustNot".equals(currentFieldName)) {
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						clauses.add(new BooleanClause(parseContext.parseInnerQuery(), BooleanClause.Occur.MUST_NOT));
					}
				} else if ("should".equals(currentFieldName)) {
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						clauses.add(new BooleanClause(parseContext.parseInnerQuery(), BooleanClause.Occur.SHOULD));
					}
				} else {
					throw new QueryParsingException(parseContext.index(), "bool query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("disable_coord".equals(currentFieldName) || "disableCoord".equals(currentFieldName)) {
					disableCoord = parser.booleanValue();
				} else if ("minimum_number_should_match".equals(currentFieldName)
						|| "minimumNumberShouldMatch".equals(currentFieldName)) {
					minimumNumberShouldMatch = parser.intValue();
				} else if ("minimum_should_match".equals(currentFieldName)
						|| "minimumShouldMatch".equals(currentFieldName)) {
					minimumNumberShouldMatch = parser.intValue();
				} else if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else {
					throw new QueryParsingException(parseContext.index(), "[bool] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (clauses.size() == 1) {
			BooleanClause clause = clauses.get(0);
			if (clause.getOccur() == BooleanClause.Occur.MUST) {
				Query query = clause.getQuery();
				query.setBoost(boost * query.getBoost());
				return query;
			}
			if (clause.getOccur() == BooleanClause.Occur.SHOULD && minimumNumberShouldMatch > 0) {
				Query query = clause.getQuery();
				query.setBoost(boost * query.getBoost());
				return query;
			}
		}

		BooleanQuery query = new BooleanQuery(disableCoord);
		for (BooleanClause clause : clauses) {
			query.add(clause);
		}
		query.setBoost(boost);
		if (minimumNumberShouldMatch != -1) {
			query.setMinimumNumberShouldMatch(minimumNumberShouldMatch);
		}
		return Queries.optimizeQuery(Queries.fixNegativeQueryIfNeeded(query));
	}
}
