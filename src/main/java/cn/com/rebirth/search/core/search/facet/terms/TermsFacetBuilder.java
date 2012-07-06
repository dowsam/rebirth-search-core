/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermsFacetBuilder.java 2012-7-6 14:29:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.terms;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilderException;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;

import com.google.common.collect.Maps;

/**
 * The Class TermsFacetBuilder.
 *
 * @author l.xue.nong
 */
public class TermsFacetBuilder extends AbstractFacetBuilder {

	/** The field name. */
	private String fieldName;

	/** The fields names. */
	private String[] fieldsNames;

	/** The size. */
	private int size = 10;

	/** The all terms. */
	private Boolean allTerms;

	/** The exclude. */
	private Object[] exclude;

	/** The regex. */
	private String regex;

	/** The regex flags. */
	private int regexFlags = 0;

	/** The comparator type. */
	private TermsFacet.ComparatorType comparatorType;

	/** The script. */
	private String script;

	/** The lang. */
	private String lang;

	/** The params. */
	private Map<String, Object> params;

	/** The execution hint. */
	String executionHint;

	/**
	 * Instantiates a new terms facet builder.
	 *
	 * @param name the name
	 */
	public TermsFacetBuilder(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#global(boolean)
	 */
	public TermsFacetBuilder global(boolean global) {
		super.global(global);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#scope(java.lang.String)
	 */
	@Override
	public TermsFacetBuilder scope(String scope) {
		super.scope(scope);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#facetFilter(cn.com.rebirth.search.core.index.query.FilterBuilder)
	 */
	public TermsFacetBuilder facetFilter(FilterBuilder filter) {
		this.facetFilter = filter;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#nested(java.lang.String)
	 */
	public TermsFacetBuilder nested(String nested) {
		this.nested = nested;
		return this;
	}

	/**
	 * Field.
	 *
	 * @param field the field
	 * @return the terms facet builder
	 */
	public TermsFacetBuilder field(String field) {
		this.fieldName = field;
		return this;
	}

	/**
	 * Fields.
	 *
	 * @param fields the fields
	 * @return the terms facet builder
	 */
	public TermsFacetBuilder fields(String... fields) {
		this.fieldsNames = fields;
		return this;
	}

	/**
	 * Script field.
	 *
	 * @param scriptField the script field
	 * @return the terms facet builder
	 */
	public TermsFacetBuilder scriptField(String scriptField) {
		this.script = scriptField;
		return this;
	}

	/**
	 * Exclude.
	 *
	 * @param exclude the exclude
	 * @return the terms facet builder
	 */
	public TermsFacetBuilder exclude(Object... exclude) {
		this.exclude = exclude;
		return this;
	}

	/**
	 * Size.
	 *
	 * @param size the size
	 * @return the terms facet builder
	 */
	public TermsFacetBuilder size(int size) {
		this.size = size;
		return this;
	}

	/**
	 * Regex.
	 *
	 * @param regex the regex
	 * @return the terms facet builder
	 */
	public TermsFacetBuilder regex(String regex) {
		return regex(regex, 0);
	}

	/**
	 * Regex.
	 *
	 * @param regex the regex
	 * @param flags the flags
	 * @return the terms facet builder
	 */
	public TermsFacetBuilder regex(String regex, int flags) {
		this.regex = regex;
		this.regexFlags = flags;
		return this;
	}

	/**
	 * Order.
	 *
	 * @param comparatorType the comparator type
	 * @return the terms facet builder
	 */
	public TermsFacetBuilder order(TermsFacet.ComparatorType comparatorType) {
		this.comparatorType = comparatorType;
		return this;
	}

	/**
	 * Script.
	 *
	 * @param script the script
	 * @return the terms facet builder
	 */
	public TermsFacetBuilder script(String script) {
		this.script = script;
		return this;
	}

	/**
	 * Lang.
	 *
	 * @param lang the lang
	 * @return the terms facet builder
	 */
	public TermsFacetBuilder lang(String lang) {
		this.lang = lang;
		return this;
	}

	/**
	 * Execution hint.
	 *
	 * @param executionHint the execution hint
	 * @return the terms facet builder
	 */
	public TermsFacetBuilder executionHint(String executionHint) {
		this.executionHint = executionHint;
		return this;
	}

	/**
	 * Param.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the terms facet builder
	 */
	public TermsFacetBuilder param(String name, Object value) {
		if (params == null) {
			params = Maps.newHashMap();
		}
		params.put(name, value);
		return this;
	}

	/**
	 * All terms.
	 *
	 * @param allTerms the all terms
	 * @return the terms facet builder
	 */
	public TermsFacetBuilder allTerms(boolean allTerms) {
		this.allTerms = allTerms;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (fieldName == null && fieldsNames == null && script == null) {
			throw new SearchSourceBuilderException("field/fields/script must be set on terms facet for facet [" + name
					+ "]");
		}
		builder.startObject(name);

		builder.startObject(TermsFacet.TYPE);
		if (fieldsNames != null) {
			if (fieldsNames.length == 1) {
				builder.field("field", fieldsNames[0]);
			} else {
				builder.field("fields", fieldsNames);
			}
		} else if (fieldName != null) {
			builder.field("field", fieldName);
		}
		builder.field("size", size);
		if (exclude != null) {
			builder.startArray("exclude");
			for (Object ex : exclude) {
				builder.value(ex);
			}
			builder.endArray();
		}
		if (regex != null) {
			builder.field("regex", regex);
			if (regexFlags != 0) {
				builder.field("regex_flags", Regex.flagsToString(regexFlags));
			}
		}
		if (comparatorType != null) {
			builder.field("order", comparatorType.name().toLowerCase());
		}
		if (allTerms != null) {
			builder.field("all_terms", allTerms);
		}

		if (script != null) {
			builder.field("script", script);
			if (lang != null) {
				builder.field("lang", lang);
			}
			if (this.params != null) {
				builder.field("params", this.params);
			}
		}

		if (executionHint != null) {
			builder.field("execution_hint", executionHint);
		}

		builder.endObject();

		addFilterFacetAndGlobal(builder, params);

		builder.endObject();
		return builder;
	}
}
