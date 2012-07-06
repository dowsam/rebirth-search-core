/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ScoreSortBuilder.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.sort;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class ScoreSortBuilder.
 *
 * @author l.xue.nong
 */
public class ScoreSortBuilder extends SortBuilder {

	/** The order. */
	private SortOrder order;

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.sort.SortBuilder#order(cn.com.rebirth.search.core.search.sort.SortOrder)
	 */
	@Override
	public ScoreSortBuilder order(SortOrder order) {
		this.order = order;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.sort.SortBuilder#missing(java.lang.Object)
	 */
	@Override
	public SortBuilder missing(Object missing) {
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject("_score");
		if (order == SortOrder.ASC) {
			builder.field("reverse", true);
		}
		builder.endObject();
		return builder;
	}
}
