/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ScriptSortBuilder.java 2012-7-6 14:29:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.sort;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

import com.google.common.collect.Maps;

/**
 * The Class ScriptSortBuilder.
 *
 * @author l.xue.nong
 */
public class ScriptSortBuilder extends SortBuilder {

	/** The lang. */
	private String lang;

	/** The script. */
	private final String script;

	/** The type. */
	private final String type;

	/** The order. */
	private SortOrder order;

	/** The params. */
	private Map<String, Object> params;

	/**
	 * Instantiates a new script sort builder.
	 *
	 * @param script the script
	 * @param type the type
	 */
	public ScriptSortBuilder(String script, String type) {
		this.script = script;
		this.type = type;
	}

	/**
	 * Param.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the script sort builder
	 */
	public ScriptSortBuilder param(String name, Object value) {
		if (params == null) {
			params = Maps.newHashMap();
		}
		params.put(name, value);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.sort.SortBuilder#order(cn.com.rebirth.search.core.search.sort.SortOrder)
	 */
	@Override
	public ScriptSortBuilder order(SortOrder order) {
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

	/**
	 * Lang.
	 *
	 * @param lang the lang
	 * @return the script sort builder
	 */
	public ScriptSortBuilder lang(String lang) {
		this.lang = lang;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject("_script");
		builder.field("script", script);
		builder.field("type", type);
		if (order == SortOrder.DESC) {
			builder.field("reverse", true);
		}
		if (lang != null) {
			builder.field("lang", lang);
		}
		if (this.params != null) {
			builder.field("params", this.params);
		}
		builder.endObject();
		return builder;
	}
}
