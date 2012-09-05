/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LimitFilterBuilder.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.commons.xcontent.XContentBuilder;

/**
 * The Class LimitFilterBuilder.
 *
 * @author l.xue.nong
 */
public class LimitFilterBuilder extends BaseFilterBuilder {

	/** The limit. */
	private final int limit;

	/**
	 * Instantiates a new limit filter builder.
	 *
	 * @param limit the limit
	 */
	public LimitFilterBuilder(int limit) {
		this.limit = limit;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(LimitFilterParser.NAME);
		builder.field("value", limit);
		builder.endObject();
	}
}