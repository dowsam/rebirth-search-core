/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MatchAllQueryBuilder.java 2012-3-29 15:02:21 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class MatchAllQueryBuilder.
 *
 * @author l.xue.nong
 */
public class MatchAllQueryBuilder extends BaseQueryBuilder {

	
	/** The norms field. */
	private String normsField;

	
	/** The boost. */
	private float boost = -1;

	
	/**
	 * Norms field.
	 *
	 * @param normsField the norms field
	 * @return the match all query builder
	 */
	public MatchAllQueryBuilder normsField(String normsField) {
		this.normsField = normsField;
		return this;
	}

	
	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the match all query builder
	 */
	public MatchAllQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(MatchAllQueryParser.NAME);
		if (boost != -1) {
			builder.field("boost", boost);
		}
		if (normsField != null) {
			builder.field("norms_field", normsField);
		}
		builder.endObject();
	}
}