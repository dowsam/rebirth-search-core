/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CustomFiltersScoreQueryBuilder.java 2012-3-29 15:00:57 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import gnu.trove.list.array.TFloatArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

import com.google.common.collect.Maps;


/**
 * The Class CustomFiltersScoreQueryBuilder.
 *
 * @author l.xue.nong
 */
public class CustomFiltersScoreQueryBuilder extends BaseQueryBuilder {

	
	/** The query builder. */
	private final QueryBuilder queryBuilder;

	
	/** The lang. */
	private String lang;

	
	/** The boost. */
	private float boost = -1;

	
	/** The params. */
	private Map<String, Object> params = null;

	
	/** The score mode. */
	private String scoreMode;

	
	/** The filters. */
	private ArrayList<FilterBuilder> filters = new ArrayList<FilterBuilder>();

	
	/** The scripts. */
	private ArrayList<String> scripts = new ArrayList<String>();

	
	/** The boosts. */
	private TFloatArrayList boosts = new TFloatArrayList();

	
	/**
	 * Instantiates a new custom filters score query builder.
	 *
	 * @param queryBuilder the query builder
	 */
	public CustomFiltersScoreQueryBuilder(QueryBuilder queryBuilder) {
		this.queryBuilder = queryBuilder;
	}

	
	/**
	 * Adds the.
	 *
	 * @param filter the filter
	 * @param script the script
	 * @return the custom filters score query builder
	 */
	public CustomFiltersScoreQueryBuilder add(FilterBuilder filter, String script) {
		this.filters.add(filter);
		this.scripts.add(script);
		this.boosts.add(-1);
		return this;
	}

	
	/**
	 * Adds the.
	 *
	 * @param filter the filter
	 * @param boost the boost
	 * @return the custom filters score query builder
	 */
	public CustomFiltersScoreQueryBuilder add(FilterBuilder filter, float boost) {
		this.filters.add(filter);
		this.scripts.add(null);
		this.boosts.add(boost);
		return this;
	}

	
	/**
	 * Score mode.
	 *
	 * @param scoreMode the score mode
	 * @return the custom filters score query builder
	 */
	public CustomFiltersScoreQueryBuilder scoreMode(String scoreMode) {
		this.scoreMode = scoreMode;
		return this;
	}

	
	/**
	 * Lang.
	 *
	 * @param lang the lang
	 * @return the custom filters score query builder
	 */
	public CustomFiltersScoreQueryBuilder lang(String lang) {
		this.lang = lang;
		return this;
	}

	
	/**
	 * Params.
	 *
	 * @param params the params
	 * @return the custom filters score query builder
	 */
	public CustomFiltersScoreQueryBuilder params(Map<String, Object> params) {
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
	 * @return the custom filters score query builder
	 */
	public CustomFiltersScoreQueryBuilder param(String key, Object value) {
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
	 * @return the custom filters score query builder
	 */
	public CustomFiltersScoreQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(CustomFiltersScoreQueryParser.NAME);
		builder.field("query");
		queryBuilder.toXContent(builder, params);

		builder.startArray("filters");
		for (int i = 0; i < filters.size(); i++) {
			builder.startObject();
			builder.field("filter");
			filters.get(i).toXContent(builder, params);
			String script = scripts.get(i);
			if (script != null) {
				builder.field("script", script);
			} else {
				builder.field("boost", boosts.get(i));
			}
			builder.endObject();
		}
		builder.endArray();

		if (scoreMode != null) {
			builder.field("score_mode", scoreMode);
		}

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