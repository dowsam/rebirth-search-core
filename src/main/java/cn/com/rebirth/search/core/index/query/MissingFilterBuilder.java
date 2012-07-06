/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MissingFilterBuilder.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class MissingFilterBuilder.
 *
 * @author l.xue.nong
 */
public class MissingFilterBuilder extends BaseFilterBuilder {

	/** The name. */
	private String name;

	/** The filter name. */
	private String filterName;

	/**
	 * Instantiates a new missing filter builder.
	 *
	 * @param name the name
	 */
	public MissingFilterBuilder(String name) {
		this.name = name;
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the missing filter builder
	 */
	public MissingFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(MissingFilterParser.NAME);
		builder.field("field", name);
		if (filterName != null) {
			builder.field("_name", filterName);
		}
		builder.endObject();
	}
}
