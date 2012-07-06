/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryFacetBuilder.java 2012-7-6 14:30:29 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.index.query.QueryBuilder;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilderException;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;

/**
 * The Class QueryFacetBuilder.
 *
 * @author l.xue.nong
 */
public class QueryFacetBuilder extends AbstractFacetBuilder {

	/** The query. */
	private QueryBuilder query;

	/**
	 * Instantiates a new query facet builder.
	 *
	 * @param name the name
	 */
	public QueryFacetBuilder(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#global(boolean)
	 */
	@Override
	public QueryFacetBuilder global(boolean global) {
		super.global(global);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#scope(java.lang.String)
	 */
	@Override
	public QueryFacetBuilder scope(String scope) {
		super.scope(scope);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#facetFilter(cn.com.rebirth.search.core.index.query.FilterBuilder)
	 */
	public QueryFacetBuilder facetFilter(FilterBuilder filter) {
		this.facetFilter = filter;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#nested(java.lang.String)
	 */
	public QueryFacetBuilder nested(String nested) {
		this.nested = nested;
		return this;
	}

	/**
	 * Query.
	 *
	 * @param query the query
	 * @return the query facet builder
	 */
	public QueryFacetBuilder query(QueryBuilder query) {
		this.query = query;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (query == null) {
			throw new SearchSourceBuilderException("query must be set on query facet for facet [" + name + "]");
		}
		builder.startObject(name);
		builder.field(QueryFacet.TYPE);
		query.toXContent(builder, params);

		addFilterFacetAndGlobal(builder, params);

		builder.endObject();
		return builder;
	}
}
