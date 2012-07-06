/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HistogramScriptFacetBuilder.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.histogram;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilderException;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;

import com.google.common.collect.Maps;

/**
 * The Class HistogramScriptFacetBuilder.
 *
 * @author l.xue.nong
 */
public class HistogramScriptFacetBuilder extends AbstractFacetBuilder {

	/** The lang. */
	private String lang;

	/** The key field name. */
	private String keyFieldName;

	/** The key script. */
	private String keyScript;

	/** The value script. */
	private String valueScript;

	/** The params. */
	private Map<String, Object> params;

	/** The interval. */
	private long interval = -1;

	/** The comparator type. */
	private HistogramFacet.ComparatorType comparatorType;

	/** The from. */
	private Object from;

	/** The to. */
	private Object to;

	/**
	 * Instantiates a new histogram script facet builder.
	 *
	 * @param name the name
	 */
	public HistogramScriptFacetBuilder(String name) {
		super(name);
	}

	/**
	 * Lang.
	 *
	 * @param lang the lang
	 * @return the histogram script facet builder
	 */
	public HistogramScriptFacetBuilder lang(String lang) {
		this.lang = lang;
		return this;
	}

	/**
	 * Key field.
	 *
	 * @param keyFieldName the key field name
	 * @return the histogram script facet builder
	 */
	public HistogramScriptFacetBuilder keyField(String keyFieldName) {
		this.keyFieldName = keyFieldName;
		return this;
	}

	/**
	 * Key script.
	 *
	 * @param keyScript the key script
	 * @return the histogram script facet builder
	 */
	public HistogramScriptFacetBuilder keyScript(String keyScript) {
		this.keyScript = keyScript;
		return this;
	}

	/**
	 * Value script.
	 *
	 * @param valueScript the value script
	 * @return the histogram script facet builder
	 */
	public HistogramScriptFacetBuilder valueScript(String valueScript) {
		this.valueScript = valueScript;
		return this;
	}

	/**
	 * Interval.
	 *
	 * @param interval the interval
	 * @return the histogram script facet builder
	 */
	public HistogramScriptFacetBuilder interval(long interval) {
		this.interval = interval;
		return this;
	}

	/**
	 * Param.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the histogram script facet builder
	 */
	public HistogramScriptFacetBuilder param(String name, Object value) {
		if (params == null) {
			params = Maps.newHashMap();
		}
		params.put(name, value);
		return this;
	}

	/**
	 * Comparator.
	 *
	 * @param comparatorType the comparator type
	 * @return the histogram script facet builder
	 */
	public HistogramScriptFacetBuilder comparator(HistogramFacet.ComparatorType comparatorType) {
		this.comparatorType = comparatorType;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#global(boolean)
	 */
	@Override
	public HistogramScriptFacetBuilder global(boolean global) {
		super.global(global);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#scope(java.lang.String)
	 */
	@Override
	public HistogramScriptFacetBuilder scope(String scope) {
		super.scope(scope);
		return this;
	}

	/**
	 * Bounds.
	 *
	 * @param from the from
	 * @param to the to
	 * @return the histogram script facet builder
	 */
	public HistogramScriptFacetBuilder bounds(Object from, Object to) {
		this.from = from;
		this.to = to;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#facetFilter(cn.com.rebirth.search.core.index.query.FilterBuilder)
	 */
	public HistogramScriptFacetBuilder facetFilter(FilterBuilder filter) {
		this.facetFilter = filter;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#nested(java.lang.String)
	 */
	public HistogramScriptFacetBuilder nested(String nested) {
		this.nested = nested;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (keyScript == null && keyFieldName == null) {
			throw new SearchSourceBuilderException(
					"key_script or key_field must be set on histogram script facet for facet [" + name + "]");
		}
		if (valueScript == null) {
			throw new SearchSourceBuilderException("value_script must be set on histogram script facet for facet ["
					+ name + "]");
		}
		builder.startObject(name);

		builder.startObject(HistogramFacet.TYPE);
		if (keyFieldName != null) {
			builder.field("key_field", keyFieldName);
		} else if (keyScript != null) {
			builder.field("key_script", keyScript);
		}
		builder.field("value_script", valueScript);

		if (from != null && to != null) {
			builder.field("from", from);
			builder.field("to", to);
		}

		if (lang != null) {
			builder.field("lang", lang);
		}
		if (interval > 0) {
			builder.field("interval", interval);
		}
		if (this.params != null) {
			builder.field("params", this.params);
		}
		if (comparatorType != null) {
			builder.field("comparator", comparatorType.description());
		}
		builder.endObject();

		addFilterFacetAndGlobal(builder, params);

		builder.endObject();
		return builder;
	}
}
