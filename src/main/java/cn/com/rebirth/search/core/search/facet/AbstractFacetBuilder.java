/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractFacetBuilder.java 2012-7-6 14:28:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.search.internal.ContextIndexSearcher;

/**
 * The Class AbstractFacetBuilder.
 *
 * @author l.xue.nong
 */
public abstract class AbstractFacetBuilder implements ToXContent {

	/** The name. */
	protected final String name;

	/** The scope. */
	protected String scope;

	/** The facet filter. */
	protected FilterBuilder facetFilter;

	/** The nested. */
	protected String nested;

	/**
	 * Instantiates a new abstract facet builder.
	 *
	 * @param name the name
	 */
	protected AbstractFacetBuilder(String name) {
		this.name = name;
	}

	/**
	 * Facet filter.
	 *
	 * @param filter the filter
	 * @return the abstract facet builder
	 */
	public AbstractFacetBuilder facetFilter(FilterBuilder filter) {
		this.facetFilter = filter;
		return this;
	}

	/**
	 * Nested.
	 *
	 * @param nested the nested
	 * @return the abstract facet builder
	 */
	public AbstractFacetBuilder nested(String nested) {
		this.nested = nested;
		return this;
	}

	/**
	 * Global.
	 *
	 * @param global the global
	 * @return the abstract facet builder
	 */
	public AbstractFacetBuilder global(boolean global) {
		this.scope = ContextIndexSearcher.Scopes.GLOBAL;
		return this;
	}

	/**
	 * Scope.
	 *
	 * @param scope the scope
	 * @return the abstract facet builder
	 */
	public AbstractFacetBuilder scope(String scope) {
		this.scope = scope;
		return this;
	}

	/**
	 * Adds the filter facet and global.
	 *
	 * @param builder the builder
	 * @param params the params
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void addFilterFacetAndGlobal(XContentBuilder builder, Params params) throws IOException {
		if (facetFilter != null) {
			builder.field("facet_filter");
			facetFilter.toXContent(builder, params);
		}

		if (nested != null) {
			builder.field("nested", nested);
		}

		if (scope != null) {
			builder.field("scope", scope);
		}
	}
}
