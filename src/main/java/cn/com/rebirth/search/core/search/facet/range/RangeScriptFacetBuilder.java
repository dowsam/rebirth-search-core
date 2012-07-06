/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RangeScriptFacetBuilder.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.range;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilderException;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The Class RangeScriptFacetBuilder.
 *
 * @author l.xue.nong
 */
public class RangeScriptFacetBuilder extends AbstractFacetBuilder {

	/** The lang. */
	private String lang;

	/** The key script. */
	private String keyScript;

	/** The value script. */
	private String valueScript;

	/** The params. */
	private Map<String, Object> params;

	/** The entries. */
	private List<Entry> entries = Lists.newArrayList();

	/**
	 * Instantiates a new range script facet builder.
	 *
	 * @param name the name
	 */
	public RangeScriptFacetBuilder(String name) {
		super(name);
	}

	/**
	 * Lang.
	 *
	 * @param lang the lang
	 * @return the range script facet builder
	 */
	public RangeScriptFacetBuilder lang(String lang) {
		this.lang = lang;
		return this;
	}

	/**
	 * Key script.
	 *
	 * @param keyScript the key script
	 * @return the range script facet builder
	 */
	public RangeScriptFacetBuilder keyScript(String keyScript) {
		this.keyScript = keyScript;
		return this;
	}

	/**
	 * Value script.
	 *
	 * @param valueScript the value script
	 * @return the range script facet builder
	 */
	public RangeScriptFacetBuilder valueScript(String valueScript) {
		this.valueScript = valueScript;
		return this;
	}

	/**
	 * Param.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the range script facet builder
	 */
	public RangeScriptFacetBuilder param(String name, Object value) {
		if (params == null) {
			params = Maps.newHashMap();
		}
		params.put(name, value);
		return this;
	}

	/**
	 * Adds the range.
	 *
	 * @param from the from
	 * @param to the to
	 * @return the range script facet builder
	 */
	public RangeScriptFacetBuilder addRange(double from, double to) {
		entries.add(new Entry(from, to));
		return this;
	}

	/**
	 * Adds the unbounded to.
	 *
	 * @param from the from
	 * @return the range script facet builder
	 */
	public RangeScriptFacetBuilder addUnboundedTo(double from) {
		entries.add(new Entry(from, Double.POSITIVE_INFINITY));
		return this;
	}

	/**
	 * Adds the unbounded from.
	 *
	 * @param to the to
	 * @return the range script facet builder
	 */
	public RangeScriptFacetBuilder addUnboundedFrom(double to) {
		entries.add(new Entry(Double.NEGATIVE_INFINITY, to));
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#global(boolean)
	 */
	public RangeScriptFacetBuilder global(boolean global) {
		super.global(global);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#scope(java.lang.String)
	 */
	@Override
	public RangeScriptFacetBuilder scope(String scope) {
		super.scope(scope);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#facetFilter(cn.com.rebirth.search.core.index.query.FilterBuilder)
	 */
	public RangeScriptFacetBuilder facetFilter(FilterBuilder filter) {
		this.facetFilter = filter;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#nested(java.lang.String)
	 */
	public RangeScriptFacetBuilder nested(String nested) {
		this.nested = nested;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (keyScript == null) {
			throw new SearchSourceBuilderException("key_script must be set on range script facet for facet [" + name
					+ "]");
		}
		if (valueScript == null) {
			throw new SearchSourceBuilderException("value_script must be set on range script facet for facet [" + name
					+ "]");
		}

		if (entries.isEmpty()) {
			throw new SearchSourceBuilderException("at least one range must be defined for range facet [" + name + "]");
		}

		builder.startObject(name);

		builder.startObject(RangeFacet.TYPE);
		builder.field("key_script", keyScript);
		builder.field("value_script", valueScript);
		if (lang != null) {
			builder.field("lang", lang);
		}

		builder.startArray("ranges");
		for (Entry entry : entries) {
			builder.startObject();
			if (!Double.isInfinite(entry.from)) {
				builder.field("from", entry.from);
			}
			if (!Double.isInfinite(entry.to)) {
				builder.field("to", entry.to);
			}
			builder.endObject();
		}
		builder.endArray();

		if (this.params != null) {
			builder.field("params", this.params);
		}
		builder.endObject();

		addFilterFacetAndGlobal(builder, params);

		builder.endObject();
		return builder;
	}

	/**
	 * The Class Entry.
	 *
	 * @author l.xue.nong
	 */
	private static class Entry {

		/** The from. */
		final double from;

		/** The to. */
		final double to;

		/**
		 * Instantiates a new entry.
		 *
		 * @param from the from
		 * @param to the to
		 */
		private Entry(double from, double to) {
			this.from = from;
			this.to = to;
		}
	}

}
