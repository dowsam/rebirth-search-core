/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesFilterBuilder.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.commons.xcontent.XContentBuilder;

/**
 * The Class IndicesFilterBuilder.
 *
 * @author l.xue.nong
 */
public class IndicesFilterBuilder extends BaseFilterBuilder {

	/** The filter builder. */
	private final FilterBuilder filterBuilder;

	/** The indices. */
	private final String[] indices;

	/** The s no match filter. */
	private String sNoMatchFilter;

	/** The no match filter. */
	private FilterBuilder noMatchFilter;

	/** The filter name. */
	private String filterName;

	/**
	 * Instantiates a new indices filter builder.
	 *
	 * @param filterBuilder the filter builder
	 * @param indices the indices
	 */
	public IndicesFilterBuilder(FilterBuilder filterBuilder, String... indices) {
		this.filterBuilder = filterBuilder;
		this.indices = indices;
	}

	/**
	 * No match filter.
	 *
	 * @param type the type
	 * @return the indices filter builder
	 */
	public IndicesFilterBuilder noMatchFilter(String type) {
		this.sNoMatchFilter = type;
		return this;
	}

	/**
	 * No match filter.
	 *
	 * @param noMatchFilter the no match filter
	 * @return the indices filter builder
	 */
	public IndicesFilterBuilder noMatchFilter(FilterBuilder noMatchFilter) {
		this.noMatchFilter = noMatchFilter;
		return this;
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the indices filter builder
	 */
	public IndicesFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(IndicesFilterParser.NAME);
		builder.field("filter");
		filterBuilder.toXContent(builder, params);
		builder.field("indices", indices);
		if (noMatchFilter != null) {
			builder.field("no_match_filter");
			noMatchFilter.toXContent(builder, params);
		} else if (sNoMatchFilter != null) {
			builder.field("no_match_filter", sNoMatchFilter);
		}

		if (filterName != null) {
			builder.field("_name", filterName);
		}

		builder.endObject();
	}
}