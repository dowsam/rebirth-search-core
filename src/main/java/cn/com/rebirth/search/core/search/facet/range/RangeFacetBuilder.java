/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RangeFacetBuilder.java 2012-7-6 14:29:10 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.range;

import java.io.IOException;
import java.util.List;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilderException;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;

import com.google.common.collect.Lists;

/**
 * The Class RangeFacetBuilder.
 *
 * @author l.xue.nong
 */
public class RangeFacetBuilder extends AbstractFacetBuilder {

	/** The key field name. */
	private String keyFieldName;

	/** The value field name. */
	private String valueFieldName;

	/** The entries. */
	private List<Entry> entries = Lists.newArrayList();

	/**
	 * Instantiates a new range facet builder.
	 *
	 * @param name the name
	 */
	public RangeFacetBuilder(String name) {
		super(name);
	}

	/**
	 * Field.
	 *
	 * @param field the field
	 * @return the range facet builder
	 */
	public RangeFacetBuilder field(String field) {
		this.keyFieldName = field;
		this.valueFieldName = field;
		return this;
	}

	/**
	 * Key field.
	 *
	 * @param keyField the key field
	 * @return the range facet builder
	 */
	public RangeFacetBuilder keyField(String keyField) {
		this.keyFieldName = keyField;
		return this;
	}

	/**
	 * Value field.
	 *
	 * @param valueField the value field
	 * @return the range facet builder
	 */
	public RangeFacetBuilder valueField(String valueField) {
		this.valueFieldName = valueField;
		return this;
	}

	/**
	 * Adds the range.
	 *
	 * @param from the from
	 * @param to the to
	 * @return the range facet builder
	 */
	public RangeFacetBuilder addRange(double from, double to) {
		entries.add(new Entry(from, to));
		return this;
	}

	/**
	 * Adds the range.
	 *
	 * @param from the from
	 * @param to the to
	 * @return the range facet builder
	 */
	public RangeFacetBuilder addRange(String from, String to) {
		entries.add(new Entry(from, to));
		return this;
	}

	/**
	 * Adds the unbounded to.
	 *
	 * @param from the from
	 * @return the range facet builder
	 */
	public RangeFacetBuilder addUnboundedTo(double from) {
		entries.add(new Entry(from, Double.POSITIVE_INFINITY));
		return this;
	}

	/**
	 * Adds the unbounded to.
	 *
	 * @param from the from
	 * @return the range facet builder
	 */
	public RangeFacetBuilder addUnboundedTo(String from) {
		entries.add(new Entry(from, null));
		return this;
	}

	/**
	 * Adds the unbounded from.
	 *
	 * @param to the to
	 * @return the range facet builder
	 */
	public RangeFacetBuilder addUnboundedFrom(double to) {
		entries.add(new Entry(Double.NEGATIVE_INFINITY, to));
		return this;
	}

	/**
	 * Adds the unbounded from.
	 *
	 * @param to the to
	 * @return the range facet builder
	 */
	public RangeFacetBuilder addUnboundedFrom(String to) {
		entries.add(new Entry(null, to));
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#global(boolean)
	 */
	public RangeFacetBuilder global(boolean global) {
		super.global(global);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#scope(java.lang.String)
	 */
	@Override
	public RangeFacetBuilder scope(String scope) {
		super.scope(scope);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#facetFilter(cn.com.rebirth.search.core.index.query.FilterBuilder)
	 */
	public RangeFacetBuilder facetFilter(FilterBuilder filter) {
		this.facetFilter = filter;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#nested(java.lang.String)
	 */
	public RangeFacetBuilder nested(String nested) {
		this.nested = nested;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (keyFieldName == null) {
			throw new SearchSourceBuilderException("field must be set on range facet for facet [" + name + "]");
		}

		if (entries.isEmpty()) {
			throw new SearchSourceBuilderException("at least one range must be defined for range facet [" + name + "]");
		}

		builder.startObject(name);

		builder.startObject(RangeFacet.TYPE);
		if (valueFieldName != null && !keyFieldName.equals(valueFieldName)) {
			builder.field("key_field", keyFieldName);
			builder.field("value_field", valueFieldName);
		} else {
			builder.field("field", keyFieldName);
		}

		builder.startArray("ranges");
		for (Entry entry : entries) {
			builder.startObject();
			if (entry.fromAsString != null) {
				builder.field("from", entry.fromAsString);
			} else if (!Double.isInfinite(entry.from)) {
				builder.field("from", entry.from);
			}
			if (entry.toAsString != null) {
				builder.field("to", entry.toAsString);
			} else if (!Double.isInfinite(entry.to)) {
				builder.field("to", entry.to);
			}
			builder.endObject();
		}
		builder.endArray();

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
	static class Entry {

		/** The from. */
		double from = Double.NEGATIVE_INFINITY;

		/** The to. */
		double to = Double.POSITIVE_INFINITY;

		/** The from as string. */
		String fromAsString;

		/** The to as string. */
		String toAsString;

		/**
		 * Instantiates a new entry.
		 *
		 * @param fromAsString the from as string
		 * @param toAsString the to as string
		 */
		Entry(String fromAsString, String toAsString) {
			this.fromAsString = fromAsString;
			this.toAsString = toAsString;
		}

		/**
		 * Instantiates a new entry.
		 *
		 * @param from the from
		 * @param to the to
		 */
		Entry(double from, double to) {
			this.from = from;
			this.to = to;
		}
	}
}
