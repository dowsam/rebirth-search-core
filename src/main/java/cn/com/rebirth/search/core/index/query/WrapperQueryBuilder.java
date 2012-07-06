/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core WrapperQueryBuilder.java 2012-7-6 14:30:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

import com.google.common.base.Charsets;

/**
 * The Class WrapperQueryBuilder.
 *
 * @author l.xue.nong
 */
public class WrapperQueryBuilder extends BaseQueryBuilder {

	/** The source. */
	private final byte[] source;

	/** The offset. */
	private final int offset;

	/** The length. */
	private final int length;

	/**
	 * Instantiates a new wrapper query builder.
	 *
	 * @param source the source
	 */
	public WrapperQueryBuilder(String source) {
		this.source = source.getBytes(Charsets.UTF_8);
		this.offset = 0;
		this.length = source.length();
	}

	/**
	 * Instantiates a new wrapper query builder.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 */
	public WrapperQueryBuilder(byte[] source, int offset, int length) {
		this.source = source;
		this.offset = offset;
		this.length = length;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(WrapperQueryParser.NAME);
		builder.field("query", source, offset, length);
		builder.endObject();
	}
}
