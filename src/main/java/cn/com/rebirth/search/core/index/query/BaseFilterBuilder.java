/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BaseFilterBuilder.java 2012-3-29 15:00:47 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class BaseFilterBuilder.
 *
 * @author l.xue.nong
 */
public abstract class BaseFilterBuilder implements FilterBuilder {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject();
		doXContent(builder, params);
		builder.endObject();
		return builder;
	}

	
	/**
	 * Do x content.
	 *
	 * @param builder the builder
	 * @param params the params
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected abstract void doXContent(XContentBuilder builder, Params params) throws IOException;
}