/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ConstantScoreQueryBuilder.java 2012-3-29 15:01:38 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class ConstantScoreQueryBuilder.
 *
 * @author l.xue.nong
 */
public class ConstantScoreQueryBuilder extends BaseQueryBuilder {

	
	/** The filter builder. */
	private final FilterBuilder filterBuilder;

	
	/** The boost. */
	private float boost = -1;

	
	/**
	 * Instantiates a new constant score query builder.
	 *
	 * @param filterBuilder the filter builder
	 */
	public ConstantScoreQueryBuilder(FilterBuilder filterBuilder) {
		this.filterBuilder = filterBuilder;
	}

	
	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the constant score query builder
	 */
	public ConstantScoreQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(ConstantScoreQueryParser.NAME);
		builder.field("filter");
		filterBuilder.toXContent(builder, params);
		if (boost != -1) {
			builder.field("boost", boost);
		}
		builder.endObject();
	}
}