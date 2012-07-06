/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CustomFiltersScoreQueryParser.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import gnu.trove.list.array.TFloatArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.function.BoostScoreFunction;
import cn.com.rebirth.search.commons.lucene.search.function.FiltersFunctionScoreQuery;
import cn.com.rebirth.search.commons.lucene.search.function.ScoreFunction;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.script.SearchScript;

/**
 * The Class CustomFiltersScoreQueryParser.
 *
 * @author l.xue.nong
 */
public class CustomFiltersScoreQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "custom_filters_score";

	/**
	 * Instantiates a new custom filters score query parser.
	 */
	@Inject
	public CustomFiltersScoreQueryParser() {
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
		String scriptLang = null;
		Map<String, Object> vars = null;

		FiltersFunctionScoreQuery.ScoreMode scoreMode = FiltersFunctionScoreQuery.ScoreMode.First;
		ArrayList<Filter> filters = new ArrayList<Filter>();
		ArrayList<String> scripts = new ArrayList<String>();
		TFloatArrayList boosts = new TFloatArrayList();

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("query".equals(currentFieldName)) {
					query = parseContext.parseInnerQuery();
				} else if ("params".equals(currentFieldName)) {
					vars = parser.map();
				} else {
					throw new QueryParsingException(parseContext.index(),
							"[custom_filters_score] query does not support [" + currentFieldName + "]");
				}
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("filters".equals(currentFieldName)) {
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						String script = null;
						Filter filter = null;
						float fboost = Float.NaN;
						while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
							if (token == XContentParser.Token.FIELD_NAME) {
								currentFieldName = parser.currentName();
							} else if (token == XContentParser.Token.START_OBJECT) {
								if ("filter".equals(currentFieldName)) {
									filter = parseContext.parseInnerFilter();
								}
							} else if (token.isValue()) {
								if ("script".equals(currentFieldName)) {
									script = parser.text();
								} else if ("boost".equals(currentFieldName)) {
									fboost = parser.floatValue();
								}
							}
						}
						if (script == null && fboost == -1) {
							throw new QueryParsingException(parseContext.index(),
									"[custom_filters_score] missing 'script' or 'boost' in filters array element");
						}
						if (filter == null) {
							throw new QueryParsingException(parseContext.index(),
									"[custom_filters_score] missing 'filter' in filters array element");
						}
						filters.add(filter);
						scripts.add(script);
						boosts.add(fboost);
					}
				} else {
					throw new QueryParsingException(parseContext.index(),
							"[custom_filters_score] query does not support [" + currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("lang".equals(currentFieldName)) {
					scriptLang = parser.text();
				} else if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else if ("score_mode".equals(currentFieldName) || "scoreMode".equals(currentFieldName)) {
					String sScoreMode = parser.text();
					if ("avg".equals(sScoreMode)) {
						scoreMode = FiltersFunctionScoreQuery.ScoreMode.Avg;
					} else if ("max".equals(sScoreMode)) {
						scoreMode = FiltersFunctionScoreQuery.ScoreMode.Max;
					} else if ("min".equals(sScoreMode)) {
						scoreMode = FiltersFunctionScoreQuery.ScoreMode.Min;
					} else if ("total".equals(sScoreMode)) {
						scoreMode = FiltersFunctionScoreQuery.ScoreMode.Total;
					} else if ("multiply".equals(sScoreMode)) {
						scoreMode = FiltersFunctionScoreQuery.ScoreMode.Multiply;
					} else if ("first".equals(sScoreMode)) {
						scoreMode = FiltersFunctionScoreQuery.ScoreMode.First;
					} else {
						throw new QueryParsingException(parseContext.index(),
								"[custom_filters_score] illegal score_mode [" + sScoreMode + "]");
					}
				} else {
					throw new QueryParsingException(parseContext.index(),
							"[custom_filters_score] query does not support [" + currentFieldName + "]");
				}
			}
		}
		if (query == null) {
			throw new QueryParsingException(parseContext.index(), "[custom_filters_score] requires 'query' field");
		}
		if (filters.isEmpty()) {
			throw new QueryParsingException(parseContext.index(), "[custom_filters_score] requires 'filters' field");
		}

		FiltersFunctionScoreQuery.FilterFunction[] filterFunctions = new FiltersFunctionScoreQuery.FilterFunction[filters
				.size()];
		for (int i = 0; i < filterFunctions.length; i++) {
			ScoreFunction scoreFunction;
			String script = scripts.get(i);
			if (script != null) {
				SearchScript searchScript = parseContext.scriptService().search(parseContext.lookup(), scriptLang,
						script, vars);
				scoreFunction = new CustomScoreQueryParser.ScriptScoreFunction(script, vars, searchScript);
			} else {
				scoreFunction = new BoostScoreFunction(boosts.get(i));
			}
			filterFunctions[i] = new FiltersFunctionScoreQuery.FilterFunction(filters.get(i), scoreFunction);
		}
		FiltersFunctionScoreQuery functionScoreQuery = new FiltersFunctionScoreQuery(query, scoreMode, filterFunctions);
		functionScoreQuery.setBoost(boost);
		return functionScoreQuery;
	}
}