/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FilterFacetBuilder.java 2012-7-6 14:28:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.filter;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilderException;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;

/**
 * The Class FilterFacetBuilder.
 *
 * @author l.xue.nong
 */
public class FilterFacetBuilder extends AbstractFacetBuilder {

	/** The filter. */
	private FilterBuilder filter;

	/**
	 * Instantiates a new filter facet builder.
	 *
	 * @param name the name
	 */
	public FilterFacetBuilder(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#global(boolean)
	 */
	@Override
	public FilterFacetBuilder global(boolean global) {
		super.global(global);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#scope(java.lang.String)
	 */
	@Override
	public FilterFacetBuilder scope(String scope) {
		super.scope(scope);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#facetFilter(cn.com.rebirth.search.core.index.query.FilterBuilder)
	 */
	public FilterFacetBuilder facetFilter(FilterBuilder filter) {
		this.facetFilter = filter;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#nested(java.lang.String)
	 */
	public FilterFacetBuilder nested(String nested) {
		this.nested = nested;
		return this;
	}

	/**
	 * Filter.
	 *
	 * @param filter the filter
	 * @return the filter facet builder
	 */
	public FilterFacetBuilder filter(FilterBuilder filter) {
		this.filter = filter;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (filter == null) {
			throw new SearchSourceBuilderException("filter must be set on filter facet for facet [" + name + "]");
		}
		builder.startObject(name);
		builder.field(FilterFacet.TYPE);
		filter.toXContent(builder, params);

		addFilterFacetAndGlobal(builder, params);

		builder.endObject();
		return builder;
	}
}
