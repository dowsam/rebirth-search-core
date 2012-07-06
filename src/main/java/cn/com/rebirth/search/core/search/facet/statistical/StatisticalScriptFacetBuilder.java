/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core StatisticalScriptFacetBuilder.java 2012-3-29 15:00:52 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.statistical;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilderException;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;

import com.google.common.collect.Maps;


/**
 * The Class StatisticalScriptFacetBuilder.
 *
 * @author l.xue.nong
 */
public class StatisticalScriptFacetBuilder extends AbstractFacetBuilder {

	
	/** The lang. */
	private String lang;

	
	/** The script. */
	private String script;

	
	/** The params. */
	private Map<String, Object> params;

	
	/**
	 * Instantiates a new statistical script facet builder.
	 *
	 * @param name the name
	 */
	public StatisticalScriptFacetBuilder(String name) {
		super(name);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetBuilder#global(boolean)
	 */
	public StatisticalScriptFacetBuilder global(boolean global) {
		super.global(global);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetBuilder#scope(java.lang.String)
	 */
	@Override
	public AbstractFacetBuilder scope(String scope) {
		super.scope(scope);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetBuilder#facetFilter(cn.com.summall.search.core.index.query.FilterBuilder)
	 */
	public StatisticalScriptFacetBuilder facetFilter(FilterBuilder filter) {
		this.facetFilter = filter;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetBuilder#nested(java.lang.String)
	 */
	public StatisticalScriptFacetBuilder nested(String nested) {
		this.nested = nested;
		return this;
	}

	
	/**
	 * Lang.
	 *
	 * @param lang the lang
	 * @return the statistical script facet builder
	 */
	public StatisticalScriptFacetBuilder lang(String lang) {
		this.lang = lang;
		return this;
	}

	
	/**
	 * Script.
	 *
	 * @param script the script
	 * @return the statistical script facet builder
	 */
	public StatisticalScriptFacetBuilder script(String script) {
		this.script = script;
		return this;
	}

	
	/**
	 * Param.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the statistical script facet builder
	 */
	public StatisticalScriptFacetBuilder param(String name, Object value) {
		if (params == null) {
			params = Maps.newHashMap();
		}
		params.put(name, value);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (script == null) {
			throw new SearchSourceBuilderException("script must be set on statistical script facet [" + name + "]");
		}
		builder.startObject(name);

		builder.startObject(StatisticalFacet.TYPE);
		builder.field("script", script);
		if (lang != null) {
			builder.field("lang", lang);
		}
		if (this.params != null) {
			builder.field("params", this.params);
		}
		builder.endObject();

		addFilterFacetAndGlobal(builder, params);

		builder.endObject();
		return builder;
	}
}
