/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NestedFilterParser.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.DeletionAwareConstantScoreQuery;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.object.ObjectMapper;
import cn.com.rebirth.search.core.index.search.nested.BlockJoinQuery;
import cn.com.rebirth.search.core.index.search.nested.NonNestedDocsFilter;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class NestedFilterParser.
 *
 * @author l.xue.nong
 */
public class NestedFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "nested";

	/**
	 * Instantiates a new nested filter parser.
	 */
	@Inject
	public NestedFilterParser() {
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
		Filter filter = null;
		float boost = 1.0f;
		String scope = null;
		String path = null;
		boolean cache = false;
		CacheKeyFilter.Key cacheKey = null;
		String filterName = null;

		NestedQueryParser.LateBindingParentFilter currentParentFilterContext = NestedQueryParser.parentFilterContext
				.get();

		NestedQueryParser.LateBindingParentFilter usAsParentFilter = new NestedQueryParser.LateBindingParentFilter();
		NestedQueryParser.parentFilterContext.set(usAsParentFilter);

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
						throw new QueryParsingException(parseContext.index(), "[nested] filter does not support ["
								+ currentFieldName + "]");
					}
				} else if (token.isValue()) {
					if ("path".equals(currentFieldName)) {
						path = parser.text();
					} else if ("boost".equals(currentFieldName)) {
						boost = parser.floatValue();
					} else if ("_scope".equals(currentFieldName)) {
						scope = parser.text();
					} else if ("_name".equals(currentFieldName)) {
						filterName = parser.text();
					} else if ("_cache".equals(currentFieldName)) {
						cache = parser.booleanValue();
					} else if ("_cache_key".equals(currentFieldName) || "_cacheKey".equals(currentFieldName)) {
						cacheKey = new CacheKeyFilter.Key(parser.text());
					} else {
						throw new QueryParsingException(parseContext.index(), "[nested] filter does not support ["
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

			query.setBoost(boost);

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

			BlockJoinQuery joinQuery = new BlockJoinQuery(query, parentFilter, BlockJoinQuery.ScoreMode.None);

			if (scope != null) {
				SearchContext.current().addNestedQuery(scope, joinQuery);
			}

			Filter joinFilter = new QueryWrapperFilter(joinQuery);
			if (cache) {
				joinFilter = parseContext.cacheFilter(joinFilter, cacheKey);
			}
			if (filterName != null) {
				parseContext.addNamedFilter(filterName, joinFilter);
			}
			return joinFilter;

		} finally {

			NestedQueryParser.parentFilterContext.set(currentParentFilterContext);
		}
	}
}
