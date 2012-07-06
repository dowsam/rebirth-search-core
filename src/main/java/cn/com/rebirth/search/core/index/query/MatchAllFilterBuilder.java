/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MatchAllFilterBuilder.java 2012-3-29 15:00:57 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class MatchAllFilterBuilder.
 *
 * @author l.xue.nong
 */
public class MatchAllFilterBuilder extends BaseFilterBuilder {

	
	/**
	 * Instantiates a new match all filter builder.
	 */
	public MatchAllFilterBuilder() {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(MatchAllFilterParser.NAME).endObject();
	}
}
