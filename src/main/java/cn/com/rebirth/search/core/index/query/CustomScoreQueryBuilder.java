/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CustomScoreQueryBuilder.java 2012-7-6 14:29:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.xcontent.XContentBuilder;

import com.google.common.collect.Maps;

/**
 * The Class CustomScoreQueryBuilder.
 *
 * @author l.xue.nong
 */
public class CustomScoreQueryBuilder extends BaseQueryBuilder {

	/** The query builder. */
	private final QueryBuilder queryBuilder;

	/** The script. */
	private String script;

	/** The lang. */
	private String lang;

	/** The boost. */
	private float boost = -1;

	/** The params. */
	private Map<String, Object> params = null;

	/**
	 * Instantiates a new custom score query builder.
	 *
	 * @param queryBuilder the query builder
	 */
	public CustomScoreQueryBuilder(QueryBuilder queryBuilder) {
		this.queryBuilder = queryBuilder;
	}

	/**
	 * Script.
	 *
	 * @param script the script
	 * @return the custom score query builder
	 */
	public CustomScoreQueryBuilder script(String script) {
		this.script = script;
		return this;
	}

	/**
	 * Lang.
	 *
	 * @param lang the lang
	 * @return the custom score query builder
	 */
	public CustomScoreQueryBuilder lang(String lang) {
		this.lang = lang;
		return this;
	}

	/**
	 * Params.
	 *
	 * @param params the params
	 * @return the custom score query builder
	 */
	public CustomScoreQueryBuilder params(Map<String, Object> params) {
		if (this.params == null) {
			this.params = params;
		} else {
			this.params.putAll(params);
		}
		return this;
	}

	/**
	 * Param.
	 *
	 * @param key the key
	 * @param value the value
	 * @return the custom score query builder
	 */
	public CustomScoreQueryBuilder param(String key, Object value) {
		if (params == null) {
			params = Maps.newHashMap();
		}
		params.put(key, value);
		return this;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the custom score query builder
	 */
	public CustomScoreQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(CustomScoreQueryParser.NAME);
		builder.field("query");
		queryBuilder.toXContent(builder, params);
		builder.field("script", script);
		if (lang != null) {
			builder.field("lang", lang);
		}
		if (this.params != null) {
			builder.field("params", this.params);
		}
		if (boost != -1) {
			builder.field("boost", boost);
		}
		builder.endObject();
	}
}