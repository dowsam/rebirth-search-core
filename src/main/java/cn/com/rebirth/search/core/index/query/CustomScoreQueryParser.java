/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CustomScoreQueryParser.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.function.FunctionScoreQuery;
import cn.com.rebirth.search.commons.lucene.search.function.ScoreFunction;
import cn.com.rebirth.search.core.script.SearchScript;

/**
 * The Class CustomScoreQueryParser.
 *
 * @author l.xue.nong
 */
public class CustomScoreQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "custom_score";

	/**
	 * Instantiates a new custom score query parser.
	 */
	@Inject
	public CustomScoreQueryParser() {
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
		String script = null;
		String scriptLang = null;
		Map<String, Object> vars = null;

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
					throw new QueryParsingException(parseContext.index(), "[custom_score] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("script".equals(currentFieldName)) {
					script = parser.text();
				} else if ("lang".equals(currentFieldName)) {
					scriptLang = parser.text();
				} else if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else {
					throw new QueryParsingException(parseContext.index(), "[custom_score] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}
		if (query == null) {
			throw new QueryParsingException(parseContext.index(), "[custom_score] requires 'query' field");
		}
		if (script == null) {
			throw new QueryParsingException(parseContext.index(), "[custom_score] requires 'script' field");
		}

		SearchScript searchScript = parseContext.scriptService()
				.search(parseContext.lookup(), scriptLang, script, vars);
		FunctionScoreQuery functionScoreQuery = new FunctionScoreQuery(query, new ScriptScoreFunction(script, vars,
				searchScript));
		functionScoreQuery.setBoost(boost);
		return functionScoreQuery;
	}

	/**
	 * The Class ScriptScoreFunction.
	 *
	 * @author l.xue.nong
	 */
	public static class ScriptScoreFunction implements ScoreFunction {

		/** The s script. */
		private final String sScript;

		/** The params. */
		private final Map<String, Object> params;

		/** The script. */
		private final SearchScript script;

		/**
		 * Instantiates a new script score function.
		 *
		 * @param sScript the s script
		 * @param params the params
		 * @param script the script
		 */
		public ScriptScoreFunction(String sScript, Map<String, Object> params, SearchScript script) {
			this.sScript = sScript;
			this.params = params;
			this.script = script;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.commons.lucene.search.function.ScoreFunction#setNextReader(org.apache.lucene.index.IndexReader)
		 */
		@Override
		public void setNextReader(IndexReader reader) {
			script.setNextReader(reader);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.commons.lucene.search.function.ScoreFunction#score(int, float)
		 */
		@Override
		public float score(int docId, float subQueryScore) {
			script.setNextDocId(docId);
			script.setNextScore(subQueryScore);
			return script.runAsFloat();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.commons.lucene.search.function.ScoreFunction#factor(int)
		 */
		@Override
		public float factor(int docId) {

			script.setNextDocId(docId);
			return script.runAsFloat();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.commons.lucene.search.function.ScoreFunction#explainScore(int, org.apache.lucene.search.Explanation)
		 */
		@Override
		public Explanation explainScore(int docId, Explanation subQueryExpl) {
			float score = score(docId, subQueryExpl.getValue());
			Explanation exp = new Explanation(score, "script score function: product of:");
			exp.addDetail(subQueryExpl);
			return exp;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.commons.lucene.search.function.ScoreFunction#explainFactor(int)
		 */
		@Override
		public Explanation explainFactor(int docId) {
			return new Explanation(factor(docId), "scriptFactor");
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "script[" + sScript + "], params [" + params + "]";
		}
	}
}