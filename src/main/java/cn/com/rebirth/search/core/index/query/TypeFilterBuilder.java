/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TypeFilterBuilder.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class TypeFilterBuilder.
 *
 * @author l.xue.nong
 */
public class TypeFilterBuilder extends BaseFilterBuilder {

	/** The type. */
	private final String type;

	/**
	 * Instantiates a new type filter builder.
	 *
	 * @param type the type
	 */
	public TypeFilterBuilder(String type) {
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(TypeFilterParser.NAME);
		builder.field("value", type);
		builder.endObject();
	}
}