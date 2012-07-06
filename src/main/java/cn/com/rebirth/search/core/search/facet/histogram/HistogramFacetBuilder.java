/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HistogramFacetBuilder.java 2012-7-6 14:29:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.histogram;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilderException;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;

/**
 * The Class HistogramFacetBuilder.
 *
 * @author l.xue.nong
 */
public class HistogramFacetBuilder extends AbstractFacetBuilder {

	/** The key field name. */
	private String keyFieldName;

	/** The value field name. */
	private String valueFieldName;

	/** The interval. */
	private long interval = -1;

	/** The comparator type. */
	private HistogramFacet.ComparatorType comparatorType;

	/** The from. */
	private Object from;

	/** The to. */
	private Object to;

	/**
	 * Instantiates a new histogram facet builder.
	 *
	 * @param name the name
	 */
	public HistogramFacetBuilder(String name) {
		super(name);
	}

	/**
	 * Field.
	 *
	 * @param field the field
	 * @return the histogram facet builder
	 */
	public HistogramFacetBuilder field(String field) {
		this.keyFieldName = field;
		return this;
	}

	/**
	 * Key field.
	 *
	 * @param keyField the key field
	 * @return the histogram facet builder
	 */
	public HistogramFacetBuilder keyField(String keyField) {
		this.keyFieldName = keyField;
		return this;
	}

	/**
	 * Value field.
	 *
	 * @param valueField the value field
	 * @return the histogram facet builder
	 */
	public HistogramFacetBuilder valueField(String valueField) {
		this.valueFieldName = valueField;
		return this;
	}

	/**
	 * Interval.
	 *
	 * @param interval the interval
	 * @return the histogram facet builder
	 */
	public HistogramFacetBuilder interval(long interval) {
		this.interval = interval;
		return this;
	}

	/**
	 * Interval.
	 *
	 * @param interval the interval
	 * @param unit the unit
	 * @return the histogram facet builder
	 */
	public HistogramFacetBuilder interval(long interval, TimeUnit unit) {
		return interval(unit.toMillis(interval));
	}

	/**
	 * Bounds.
	 *
	 * @param from the from
	 * @param to the to
	 * @return the histogram facet builder
	 */
	public HistogramFacetBuilder bounds(Object from, Object to) {
		this.from = from;
		this.to = to;
		return this;
	}

	/**
	 * Comparator.
	 *
	 * @param comparatorType the comparator type
	 * @return the histogram facet builder
	 */
	public HistogramFacetBuilder comparator(HistogramFacet.ComparatorType comparatorType) {
		this.comparatorType = comparatorType;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#global(boolean)
	 */
	public HistogramFacetBuilder global(boolean global) {
		super.global(global);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#scope(java.lang.String)
	 */
	@Override
	public HistogramFacetBuilder scope(String scope) {
		super.scope(scope);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#facetFilter(cn.com.rebirth.search.core.index.query.FilterBuilder)
	 */
	public HistogramFacetBuilder facetFilter(FilterBuilder filter) {
		this.facetFilter = filter;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#nested(java.lang.String)
	 */
	public HistogramFacetBuilder nested(String nested) {
		this.nested = nested;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (keyFieldName == null) {
			throw new SearchSourceBuilderException("field must be set on histogram facet for facet [" + name + "]");
		}
		if (interval < 0) {
			throw new SearchSourceBuilderException("interval must be set on histogram facet for facet [" + name + "]");
		}
		builder.startObject(name);

		builder.startObject(HistogramFacet.TYPE);
		if (valueFieldName != null) {
			builder.field("key_field", keyFieldName);
			builder.field("value_field", valueFieldName);
		} else {
			builder.field("field", keyFieldName);
		}
		builder.field("interval", interval);

		if (from != null && to != null) {
			builder.field("from", from);
			builder.field("to", to);
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
