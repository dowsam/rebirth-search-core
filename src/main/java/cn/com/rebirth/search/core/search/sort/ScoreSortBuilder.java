/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ScoreSortBuilder.java 2012-3-29 15:00:58 l.xue.nong$$
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
	 * @see cn.com.summall.search.core.search.sort.SortBuilder#order(cn.com.summall.search.core.search.sort.SortOrder)
	 */
	@Override
	public ScoreSortBuilder order(SortOrder order) {
		this.order = order;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.sort.SortBuilder#missing(java.lang.Object)
	 */
	@Override
	public SortBuilder missing(Object missing) {
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
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
