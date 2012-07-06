/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesQueryParser.java 2012-3-29 15:02:42 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.MatchNoDocsQuery;
import cn.com.rebirth.search.commons.lucene.search.Queries;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;

import com.google.common.collect.Sets;


/**
 * The Class IndicesQueryParser.
 *
 * @author l.xue.nong
 */
public class IndicesQueryParser implements QueryParser {

	
	/** The Constant NAME. */
	public static final String NAME = "indices";

	
	/** The cluster service. */
	@Nullable
	private final ClusterService clusterService;

	
	/**
	 * Instantiates a new indices query parser.
	 *
	 * @param clusterService the cluster service
	 */
	@Inject
	public IndicesQueryParser(@Nullable ClusterService clusterService) {
		this.clusterService = clusterService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryParser#parse(cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		Query query = null;
		Set<String> indices = Sets.newHashSet();

		String currentFieldName = null;
		XContentParser.Token token;
		Query noMatchQuery = Queries.MATCH_ALL_QUERY;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("query".equals(currentFieldName)) {
					query = parseContext.parseInnerQuery();
				} else if ("no_match_query".equals(currentFieldName)) {
					noMatchQuery = parseContext.parseInnerQuery();
				} else {
					throw new QueryParsingException(parseContext.index(), "[indices] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("indices".equals(currentFieldName)) {
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						String value = parser.textOrNull();
						if (value == null) {
							throw new QueryParsingException(parseContext.index(), "No value specified for term filter");
						}
						indices.add(value);
					}
				} else {
					throw new QueryParsingException(parseContext.index(), "[indices] query does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("index".equals(currentFieldName)) {
					indices.add(parser.text());
				} else if ("no_match_query".equals(currentFieldName)) {
					String type = parser.text();
					if ("all".equals(type)) {
						noMatchQuery = Queries.MATCH_ALL_QUERY;
					} else if ("none".equals(type)) {
						noMatchQuery = MatchNoDocsQuery.INSTANCE;
					}
				} else {
					throw new QueryParsingException(parseContext.index(), "[indices] query does not support ["
							+ currentFieldName + "]");
				}
			}
		}
		if (query == null) {
			throw new QueryParsingException(parseContext.index(), "[indices] requires 'query' element");
		}
		if (indices.isEmpty()) {
			throw new QueryParsingException(parseContext.index(), "[indices] requires 'indices' element");
		}

		String[] concreteIndices = indices.toArray(new String[indices.size()]);
		if (clusterService != null) {
			MetaData metaData = clusterService.state().metaData();
			concreteIndices = metaData.concreteIndices(indices.toArray(new String[indices.size()]), true, true);
		}

		for (String index : concreteIndices) {
			if (Regex.simpleMatch(index, parseContext.index().name())) {
				return query;
			}
		}
		return noMatchQuery;
	}
}
