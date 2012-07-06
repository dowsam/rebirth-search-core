/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermsStatsFacetBuilder.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.termsstats;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilderException;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;

import com.google.common.collect.Maps;

/**
 * The Class TermsStatsFacetBuilder.
 *
 * @author l.xue.nong
 */
public class TermsStatsFacetBuilder extends AbstractFacetBuilder {

	/** The key field. */
	private String keyField;

	/** The value field. */
	private String valueField;

	/** The size. */
	private int size = -1;

	/** The comparator type. */
	private TermsStatsFacet.ComparatorType comparatorType;

	/** The script. */
	private String script;

	/** The lang. */
	private String lang;

	/** The params. */
	private Map<String, Object> params;

	/**
	 * Instantiates a new terms stats facet builder.
	 *
	 * @param name the name
	 */
	public TermsStatsFacetBuilder(String name) {
		super(name);
	}

	/**
	 * Key field.
	 *
	 * @param keyField the key field
	 * @return the terms stats facet builder
	 */
	public TermsStatsFacetBuilder keyField(String keyField) {
		this.keyField = keyField;
		return this;
	}

	/**
	 * Value field.
	 *
	 * @param valueField the value field
	 * @return the terms stats facet builder
	 */
	public TermsStatsFacetBuilder valueField(String valueField) {
		this.valueField = valueField;
		return this;
	}

	/**
	 * Order.
	 *
	 * @param comparatorType the comparator type
	 * @return the terms stats facet builder
	 */
	public TermsStatsFacetBuilder order(TermsStatsFacet.ComparatorType comparatorType) {
		this.comparatorType = comparatorType;
		return this;
	}

	/**
	 * Size.
	 *
	 * @param size the size
	 * @return the terms stats facet builder
	 */
	public TermsStatsFacetBuilder size(int size) {
		this.size = size;
		return this;
	}

	/**
	 * All terms.
	 *
	 * @return the terms stats facet builder
	 */
	public TermsStatsFacetBuilder allTerms() {
		this.size = 0;
		return this;
	}

	/**
	 * Value script.
	 *
	 * @param script the script
	 * @return the terms stats facet builder
	 */
	public TermsStatsFacetBuilder valueScript(String script) {
		this.script = script;
		return this;
	}

	/**
	 * Lang.
	 *
	 * @param lang the lang
	 * @return the terms stats facet builder
	 */
	public TermsStatsFacetBuilder lang(String lang) {
		this.lang = lang;
		return this;
	}

	/**
	 * Param.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the terms stats facet builder
	 */
	public TermsStatsFacetBuilder param(String name, Object value) {
		if (params == null) {
			params = Maps.newHashMap();
		}
		params.put(name, value);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (keyField == null) {
			throw new SearchSourceBuilderException("key field must be set on terms facet for facet [" + name + "]");
		}
		if (valueField == null && script == null) {
			throw new SearchSourceBuilderException("value field or value script must be set on terms facet for facet ["
					+ name + "]");
		}
		builder.startObject(name);

		builder.startObject(TermsStatsFacet.TYPE);
		builder.field("key_field", keyField);
		if (valueField != null) {
			builder.field("value_field", valueField);
		}
		if (script != null) {
			builder.field("value_script", script);
			if (lang != null) {
				builder.field("lang", lang);
			}
			if (this.params != null) {
				builder.field("params", this.params);
			}
		}

		if (comparatorType != null) {
			builder.field("order", comparatorType.name().toLowerCase());
		}

		if (size != -1) {
			builder.field("size", size);
		}

		builder.endObject();

		addFilterFacetAndGlobal(builder, params);

		builder.endObject();

		return builder;
	}
}