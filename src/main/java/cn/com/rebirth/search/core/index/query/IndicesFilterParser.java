/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesFilterParser.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.Queries;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;

import com.google.common.collect.Sets;

/**
 * The Class IndicesFilterParser.
 *
 * @author l.xue.nong
 */
public class IndicesFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "indices";

	/** The cluster service. */
	@Nullable
	private final ClusterService clusterService;

	/**
	 * Instantiates a new indices filter parser.
	 *
	 * @param clusterService the cluster service
	 */
	@Inject
	public IndicesFilterParser(@Nullable ClusterService clusterService) {
		this.clusterService = clusterService;
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

		Filter filter = null;
		Set<String> indices = Sets.newHashSet();

		String currentFieldName = null;
		XContentParser.Token token;
		Filter noMatchFilter = Queries.MATCH_ALL_FILTER;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("filter".equals(currentFieldName)) {
					filter = parseContext.parseInnerFilter();
				} else if ("no_match_filter".equals(currentFieldName)) {
					noMatchFilter = parseContext.parseInnerFilter();
				} else {
					throw new QueryParsingException(parseContext.index(), "[indices] filter does not support ["
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
					throw new QueryParsingException(parseContext.index(), "[indices] filter does not support ["
							+ currentFieldName + "]");
				}
			} else if (token.isValue()) {
				if ("index".equals(currentFieldName)) {
					indices.add(parser.text());
				} else if ("no_match_filter".equals(currentFieldName)) {
					String type = parser.text();
					if ("all".equals(type)) {
						noMatchFilter = Queries.MATCH_ALL_FILTER;
					} else if ("none".equals(type)) {
						noMatchFilter = Queries.MATCH_NO_FILTER;
					}
				} else {
					throw new QueryParsingException(parseContext.index(), "[indices] filter does not support ["
							+ currentFieldName + "]");
				}
			}
		}
		if (filter == null) {
			throw new QueryParsingException(parseContext.index(), "[indices] requires 'filter' element");
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
				return filter;
			}
		}
		return noMatchFilter;
	}
}
