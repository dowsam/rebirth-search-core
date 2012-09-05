/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core WildcardQueryBuilder.java 2012-7-6 14:30:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.commons.xcontent.XContentBuilder;

/**
 * The Class WildcardQueryBuilder.
 *
 * @author l.xue.nong
 */
public class WildcardQueryBuilder extends BaseQueryBuilder {

	/** The name. */
	private final String name;

	/** The wildcard. */
	private final String wildcard;

	/** The boost. */
	private float boost = -1;

	/** The rewrite. */
	private String rewrite;

	/**
	 * Instantiates a new wildcard query builder.
	 *
	 * @param name the name
	 * @param wildcard the wildcard
	 */
	public WildcardQueryBuilder(String name, String wildcard) {
		this.name = name;
		this.wildcard = wildcard;
	}

	/**
	 * Rewrite.
	 *
	 * @param rewrite the rewrite
	 * @return the wildcard query builder
	 */
	public WildcardQueryBuilder rewrite(String rewrite) {
		this.rewrite = rewrite;
		return this;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the wildcard query builder
	 */
	public WildcardQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(WildcardQueryParser.NAME);
		if (boost == -1 && rewrite != null) {
			builder.field(name, wildcard);
		} else {
			builder.startObject(name);
			builder.field("wildcard", wildcard);
			if (boost != -1) {
				builder.field("boost", boost);
			}
			if (rewrite != null) {
				builder.field("rewrite", rewrite);
			}
			builder.endObject();
		}
		builder.endObject();
	}
}