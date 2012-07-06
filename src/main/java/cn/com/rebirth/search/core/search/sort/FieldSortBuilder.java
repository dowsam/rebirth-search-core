/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FieldSortBuilder.java 2012-7-6 14:30:36 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.sort;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class FieldSortBuilder.
 *
 * @author l.xue.nong
 */
public class FieldSortBuilder extends SortBuilder {

	/** The field name. */
	private final String fieldName;

	/** The order. */
	private SortOrder order;

	/** The missing. */
	private Object missing;

	/** The ignore unampped. */
	private Boolean ignoreUnampped;

	/**
	 * Instantiates a new field sort builder.
	 *
	 * @param fieldName the field name
	 */
	public FieldSortBuilder(String fieldName) {
		this.fieldName = fieldName;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.sort.SortBuilder#order(cn.com.rebirth.search.core.search.sort.SortOrder)
	 */
	@Override
	public FieldSortBuilder order(SortOrder order) {
		this.order = order;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.sort.SortBuilder#missing(java.lang.Object)
	 */
	@Override
	public FieldSortBuilder missing(Object missing) {
		this.missing = missing;
		return this;
	}

	/**
	 * Ignore unmapped.
	 *
	 * @param ignoreUnmapped the ignore unmapped
	 * @return the field sort builder
	 */
	public FieldSortBuilder ignoreUnmapped(boolean ignoreUnmapped) {
		this.ignoreUnampped = ignoreUnmapped;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(fieldName);
		if (order != null) {
			builder.field("order", order.toString());
		}
		if (missing != null) {
			builder.field("missing", missing);
		}
		if (ignoreUnampped != null) {
			builder.field("ignore_unmapped", ignoreUnampped);
		}
		builder.endObject();
		return builder;
	}
}
