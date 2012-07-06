/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HasChildFilterBuilder.java 2012-7-6 14:30:13 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class HasChildFilterBuilder.
 *
 * @author l.xue.nong
 */
public class HasChildFilterBuilder extends BaseFilterBuilder {

	/** The query builder. */
	private final QueryBuilder queryBuilder;

	/** The child type. */
	private String childType;

	/** The scope. */
	private String scope;

	/** The filter name. */
	private String filterName;

	/**
	 * Instantiates a new checks for child filter builder.
	 *
	 * @param type the type
	 * @param queryBuilder the query builder
	 */
	public HasChildFilterBuilder(String type, QueryBuilder queryBuilder) {
		this.childType = type;
		this.queryBuilder = queryBuilder;
	}

	/**
	 * Scope.
	 *
	 * @param scope the scope
	 * @return the checks for child filter builder
	 */
	public HasChildFilterBuilder scope(String scope) {
		this.scope = scope;
		return this;
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the checks for child filter builder
	 */
	public HasChildFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(HasChildFilterParser.NAME);
		builder.field("query");
		queryBuilder.toXContent(builder, params);
		builder.field("type", childType);
		if (scope != null) {
			builder.field("_scope", scope);
		}
		if (filterName != null) {
			builder.field("_name", filterName);
		}
		builder.endObject();
	}
}
