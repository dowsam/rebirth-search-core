/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ScriptFilterBuilder.java 2012-7-6 14:28:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class ScriptFilterBuilder.
 *
 * @author l.xue.nong
 */
public class ScriptFilterBuilder extends BaseFilterBuilder {

	/** The script. */
	private final String script;

	/** The params. */
	private Map<String, Object> params;

	/** The lang. */
	private String lang;

	/** The cache. */
	private Boolean cache;

	/** The cache key. */
	private String cacheKey;

	/** The filter name. */
	private String filterName;

	/**
	 * Instantiates a new script filter builder.
	 *
	 * @param script the script
	 */
	public ScriptFilterBuilder(String script) {
		this.script = script;
	}

	/**
	 * Adds the param.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the script filter builder
	 */
	public ScriptFilterBuilder addParam(String name, Object value) {
		if (params == null) {
			params = newHashMap();
		}
		params.put(name, value);
		return this;
	}

	/**
	 * Params.
	 *
	 * @param params the params
	 * @return the script filter builder
	 */
	public ScriptFilterBuilder params(Map<String, Object> params) {
		if (this.params == null) {
			this.params = params;
		} else {
			this.params.putAll(params);
		}
		return this;
	}

	/**
	 * Lang.
	 *
	 * @param lang the lang
	 * @return the script filter builder
	 */
	public ScriptFilterBuilder lang(String lang) {
		this.lang = lang;
		return this;
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the script filter builder
	 */
	public ScriptFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the script filter builder
	 */
	public ScriptFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the script filter builder
	 */
	public ScriptFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(ScriptFilterParser.NAME);
		builder.field("script", script);
		if (this.params != null) {
			builder.field("params", this.params);
		}
		if (this.lang != null) {
			builder.field("lang", lang);
		}
		if (filterName != null) {
			builder.field("_name", filterName);
		}
		if (cache != null) {
			builder.field("_cache", cache);
		}
		if (cacheKey != null) {
			builder.field("_cache_key", cacheKey);
		}
		builder.endObject();
	}
}