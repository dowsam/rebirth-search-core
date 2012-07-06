/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TypeFilterBuilder.java 2012-3-29 15:01:32 l.xue.nong$$
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
	 * @see cn.com.summall.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(TypeFilterParser.NAME);
		builder.field("value", type);
		builder.endObject();
	}
}