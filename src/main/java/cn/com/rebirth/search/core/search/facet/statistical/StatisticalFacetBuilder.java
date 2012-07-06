/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core StatisticalFacetBuilder.java 2012-3-29 15:01:58 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.statistical;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilderException;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;


/**
 * The Class StatisticalFacetBuilder.
 *
 * @author l.xue.nong
 */
public class StatisticalFacetBuilder extends AbstractFacetBuilder {

	
	/** The fields names. */
	private String[] fieldsNames;

	
	/** The field name. */
	private String fieldName;

	
	/**
	 * Instantiates a new statistical facet builder.
	 *
	 * @param name the name
	 */
	public StatisticalFacetBuilder(String name) {
		super(name);
	}

	
	/**
	 * Field.
	 *
	 * @param field the field
	 * @return the statistical facet builder
	 */
	public StatisticalFacetBuilder field(String field) {
		this.fieldName = field;
		return this;
	}

	
	/**
	 * Fields.
	 *
	 * @param fields the fields
	 * @return the statistical facet builder
	 */
	public StatisticalFacetBuilder fields(String... fields) {
		this.fieldsNames = fields;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetBuilder#global(boolean)
	 */
	public StatisticalFacetBuilder global(boolean global) {
		super.global(global);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetBuilder#scope(java.lang.String)
	 */
	@Override
	public StatisticalFacetBuilder scope(String scope) {
		super.scope(scope);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetBuilder#facetFilter(cn.com.summall.search.core.index.query.FilterBuilder)
	 */
	public StatisticalFacetBuilder facetFilter(FilterBuilder filter) {
		this.facetFilter = filter;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetBuilder#nested(java.lang.String)
	 */
	public StatisticalFacetBuilder nested(String nested) {
		this.nested = nested;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (fieldName == null && fieldsNames == null) {
			throw new SearchSourceBuilderException("field must be set on statistical facet for facet [" + name + "]");
		}
		builder.startObject(name);

		builder.startObject(StatisticalFacet.TYPE);
		if (fieldsNames != null) {
			if (fieldsNames.length == 1) {
				builder.field("field", fieldsNames[0]);
			} else {
				builder.field("fields", fieldsNames);
			}
		} else {
			builder.field("field", fieldName);
		}
		builder.endObject();

		addFilterFacetAndGlobal(builder, params);

		builder.endObject();
		return builder;
	}
}
