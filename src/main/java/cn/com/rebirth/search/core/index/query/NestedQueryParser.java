/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NestedQueryParser.java 2012-3-29 15:01:58 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.DeletionAwareConstantScoreQuery;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.object.ObjectMapper;
import cn.com.rebirth.search.core.index.search.nested.BlockJoinQuery;
import cn.com.rebirth.search.core.index.search.nested.NonNestedDocsFilter;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class NestedQueryParser.
 *
 * @author l.xue.nong
 */
public class NestedQueryParser implements QueryParser {

	
	/** The Constant NAME. */
	public static final String NAME = "nested";

	
	/**
	 * Instantiates a new nested query parser.
	 */
	@Inject
	public NestedQueryParser() {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, Strings.toCamelCase(NAME) };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryParser#parse(cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		Query query = null;
		Filter filter = null;
		float boost = 1.0f;
		String scope = null;
		String path = null;
		BlockJoinQuery.ScoreMode scoreMode = BlockJoinQuery.ScoreMode.Avg;

		
		LateBindingParentFilter currentParentFilterContext = parentFilterContext.get();

		LateBindingParentFilter usAsParentFilter = new LateBindingParentFilter();
		parentFilterContext.set(usAsParentFilter);

		try {
			String currentFieldName = null;
			XContentParser.Token token;
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else if (token == XContentParser.Token.START_OBJECT) {
					if ("query".equals(currentFieldName)) {
						query = parseContext.parseInnerQuery();
					} else if ("filter".equals(currentFieldName)) {
						filter = parseContext.parseInnerFilter();
					} else {
						throw new QueryParsingException(parseContext.index(), "[nested] query does not support ["
								+ currentFieldName + "]");
					}
				} else if (token.isValue()) {
					if ("path".equals(currentFieldName)) {
						path = parser.text();
					} else if ("boost".equals(currentFieldName)) {
						boost = parser.floatValue();
					} else if ("_scope".equals(currentFieldName)) {
						scope = parser.text();
					} else if ("score_mode".equals(currentFieldName) || "scoreMode".equals(currentFieldName)) {
						String sScoreMode = parser.text();
						if ("avg".equals(sScoreMode)) {
							scoreMode = BlockJoinQuery.ScoreMode.Avg;
						} else if ("max".equals(sScoreMode)) {
							scoreMode = BlockJoinQuery.ScoreMode.Max;
						} else if ("total".equals(sScoreMode)) {
							scoreMode = BlockJoinQuery.ScoreMode.Total;
						} else if ("none".equals(sScoreMode)) {
							scoreMode = BlockJoinQuery.ScoreMode.None;
						} else {
							throw new QueryParsingException(parseContext.index(),
									"illegal score_mode for nested query [" + sScoreMode + "]");
						}
					} else {
						throw new QueryParsingException(parseContext.index(), "[nested] query does not support ["
								+ currentFieldName + "]");
					}
				}
			}
			if (query == null && filter == null) {
				throw new QueryParsingException(parseContext.index(),
						"[nested] requires either 'query' or 'filter' field");
			}
			if (path == null) {
				throw new QueryParsingException(parseContext.index(), "[nested] requires 'path' field");
			}

			if (filter != null) {
				query = new DeletionAwareConstantScoreQuery(filter);
			}

			MapperService.SmartNameObjectMapper mapper = parseContext.smartObjectMapper(path);
			if (mapper == null) {
				throw new QueryParsingException(parseContext.index(),
						"[nested] failed to find nested object under path [" + path + "]");
			}
			ObjectMapper objectMapper = mapper.mapper();
			if (objectMapper == null) {
				throw new QueryParsingException(parseContext.index(),
						"[nested] failed to find nested object under path [" + path + "]");
			}
			if (!objectMapper.nested().isNested()) {
				throw new QueryParsingException(parseContext.index(), "[nested] nested object under path [" + path
						+ "] is not of nested type");
			}

			Filter childFilter = parseContext.cacheFilter(objectMapper.nestedTypeFilter(), null);
			usAsParentFilter.filter = childFilter;
			
			query = new FilteredQuery(query, childFilter);

			Filter parentFilter = currentParentFilterContext;
			if (parentFilter == null) {
				parentFilter = NonNestedDocsFilter.INSTANCE;
				
				
				
				
				
				parentFilter = parseContext.cacheFilter(parentFilter, null);
			}

			BlockJoinQuery joinQuery = new BlockJoinQuery(query, parentFilter, scoreMode);
			joinQuery.setBoost(boost);

			if (scope != null) {
				SearchContext.current().addNestedQuery(scope, joinQuery);
			}

			return joinQuery;
		} finally {
			
			parentFilterContext.set(currentParentFilterContext);
		}
	}

	
	/** The parent filter context. */
	static ThreadLocal<LateBindingParentFilter> parentFilterContext = new ThreadLocal<LateBindingParentFilter>();

	
	/**
	 * The Class LateBindingParentFilter.
	 *
	 * @author l.xue.nong
	 */
	static class LateBindingParentFilter extends Filter {

		
		/** The filter. */
		Filter filter;

		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return filter.hashCode();
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return filter.equals(obj);
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return filter.toString();
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
		 */
		@Override
		public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
			return filter.getDocIdSet(reader);
		}
	}
}
