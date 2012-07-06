/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ScriptFilterParser.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.docset.GetDocSet;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;
import cn.com.rebirth.search.core.script.ScriptService;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.Maps;

/**
 * The Class ScriptFilterParser.
 *
 * @author l.xue.nong
 */
public class ScriptFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "script";

	/**
	 * Instantiates a new script filter parser.
	 */
	@Inject
	public ScriptFilterParser() {
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

		XContentParser.Token token;

		boolean cache = false;
		CacheKeyFilter.Key cacheKey = null;

		String script = null;
		String scriptLang = null;
		Map<String, Object> params = null;

		String filterName = null;
		String currentFieldName = null;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("params".equals(currentFieldName)) {
					params = parser.map();
				} else {
					throw new QueryParsingException(parseContext.index(), "[script] filter does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("script".equals(currentFieldName)) {
					script = parser.text();
				} else if ("lang".equals(currentFieldName)) {
					scriptLang = parser.text();
				} else if ("_name".equals(currentFieldName)) {
					filterName = parser.text();
				} else if ("_cache".equals(currentFieldName)) {
					cache = parser.booleanValue();
				} else if ("_cache_key".equals(currentFieldName) || "_cacheKey".equals(currentFieldName)) {
					cacheKey = new CacheKeyFilter.Key(parser.text());
				} else {
					throw new QueryParsingException(parseContext.index(), "[script] filter does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (script == null) {
			throw new QueryParsingException(parseContext.index(), "script must be provided with a [script] filter");
		}
		if (params == null) {
			params = Maps.newHashMap();
		}

		Filter filter = new ScriptFilter(scriptLang, script, params, parseContext.scriptService());
		if (cache) {
			filter = parseContext.cacheFilter(filter, cacheKey);
		}
		if (filterName != null) {
			parseContext.addNamedFilter(filterName, filter);
		}
		return filter;
	}

	/**
	 * The Class ScriptFilter.
	 *
	 * @author l.xue.nong
	 */
	public static class ScriptFilter extends Filter {

		/** The script. */
		private final String script;

		/** The params. */
		private final Map<String, Object> params;

		/** The search script. */
		private final SearchScript searchScript;

		/**
		 * Instantiates a new script filter.
		 *
		 * @param scriptLang the script lang
		 * @param script the script
		 * @param params the params
		 * @param scriptService the script service
		 */
		private ScriptFilter(String scriptLang, String script, Map<String, Object> params, ScriptService scriptService) {
			this.script = script;
			this.params = params;

			SearchContext context = SearchContext.current();
			if (context == null) {
				throw new RebirthIllegalStateException("No search context on going...");
			}

			this.searchScript = context.scriptService().search(context.lookup(), scriptLang, script, params);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("ScriptFilter(");
			buffer.append(script);
			buffer.append(")");
			return buffer.toString();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			ScriptFilter that = (ScriptFilter) o;

			if (params != null ? !params.equals(that.params) : that.params != null)
				return false;
			if (script != null ? !script.equals(that.script) : that.script != null)
				return false;

			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			int result = script != null ? script.hashCode() : 0;
			result = 31 * result + (params != null ? params.hashCode() : 0);
			return result;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
		 */
		@Override
		public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
			searchScript.setNextReader(reader);
			return new ScriptDocSet(reader, searchScript);
		}

		/**
		 * The Class ScriptDocSet.
		 *
		 * @author l.xue.nong
		 */
		static class ScriptDocSet extends GetDocSet {

			/** The search script. */
			private final SearchScript searchScript;

			/**
			 * Instantiates a new script doc set.
			 *
			 * @param reader the reader
			 * @param searchScript the search script
			 */
			public ScriptDocSet(IndexReader reader, SearchScript searchScript) {
				super(reader.maxDoc());
				this.searchScript = searchScript;
			}

			/* (non-Javadoc)
			 * @see cn.com.rebirth.search.commons.lucene.docset.GetDocSet#sizeInBytes()
			 */
			@Override
			public long sizeInBytes() {
				return 0;
			}

			/* (non-Javadoc)
			 * @see org.apache.lucene.search.DocIdSet#isCacheable()
			 */
			@Override
			public boolean isCacheable() {

				return false;
			}

			/* (non-Javadoc)
			 * @see cn.com.rebirth.search.commons.lucene.docset.DocSet#get(int)
			 */
			@Override
			public boolean get(int doc) {
				searchScript.setNextDocId(doc);
				Object val = searchScript.run();
				if (val == null) {
					return false;
				}
				if (val instanceof Boolean) {
					return (Boolean) val;
				}
				if (val instanceof Number) {
					return ((Number) val).longValue() != 0;
				}
				throw new RebirthIllegalArgumentException("Can't handle type [" + val + "] in script filter");
			}
		}
	}
}