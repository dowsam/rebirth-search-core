/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PrefixQueryBuilder.java 2012-7-6 14:29:23 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.commons.xcontent.XContentBuilder;

/**
 * The Class PrefixQueryBuilder.
 *
 * @author l.xue.nong
 */
public class PrefixQueryBuilder extends BaseQueryBuilder {

	/** The name. */
	private final String name;

	/** The prefix. */
	private final String prefix;

	/** The boost. */
	private float boost = -1;

	/** The rewrite. */
	private String rewrite;

	/**
	 * Instantiates a new prefix query builder.
	 *
	 * @param name the name
	 * @param prefix the prefix
	 */
	public PrefixQueryBuilder(String name, String prefix) {
		this.name = name;
		this.prefix = prefix;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the prefix query builder
	 */
	public PrefixQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/**
	 * Rewrite.
	 *
	 * @param rewrite the rewrite
	 * @return the prefix query builder
	 */
	public PrefixQueryBuilder rewrite(String rewrite) {
		this.rewrite = rewrite;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(PrefixQueryParser.NAME);
		if (boost == -1 && rewrite == null) {
			builder.field(name, prefix);
		} else {
			builder.startObject(name);
			builder.field("prefix", prefix);
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