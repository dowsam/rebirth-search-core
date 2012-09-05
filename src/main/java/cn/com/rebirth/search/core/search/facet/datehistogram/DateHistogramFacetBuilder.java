/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DateHistogramFacetBuilder.java 2012-7-6 14:30:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.datehistogram;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilderException;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;

import com.google.common.collect.Maps;

/**
 * The Class DateHistogramFacetBuilder.
 *
 * @author l.xue.nong
 */
public class DateHistogramFacetBuilder extends AbstractFacetBuilder {

	/** The key field name. */
	private String keyFieldName;

	/** The value field name. */
	private String valueFieldName;

	/** The interval. */
	private String interval = null;

	/** The pre zone. */
	private String preZone = null;

	/** The post zone. */
	private String postZone = null;

	/** The pre zone adjust large interval. */
	private Boolean preZoneAdjustLargeInterval;

	/** The pre offset. */
	long preOffset = 0;

	/** The post offset. */
	long postOffset = 0;

	/** The factor. */
	float factor = 1.0f;

	/** The comparator type. */
	private DateHistogramFacet.ComparatorType comparatorType;

	/** The value script. */
	private String valueScript;

	/** The params. */
	private Map<String, Object> params;

	/** The lang. */
	private String lang;

	/**
	 * Instantiates a new date histogram facet builder.
	 *
	 * @param name the name
	 */
	public DateHistogramFacetBuilder(String name) {
		super(name);
	}

	/**
	 * Field.
	 *
	 * @param field the field
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder field(String field) {
		this.keyFieldName = field;
		return this;
	}

	/**
	 * Key field.
	 *
	 * @param keyField the key field
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder keyField(String keyField) {
		this.keyFieldName = keyField;
		return this;
	}

	/**
	 * Value field.
	 *
	 * @param valueField the value field
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder valueField(String valueField) {
		this.valueFieldName = valueField;
		return this;
	}

	/**
	 * Value script.
	 *
	 * @param valueScript the value script
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder valueScript(String valueScript) {
		this.valueScript = valueScript;
		return this;
	}

	/**
	 * Param.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder param(String name, Object value) {
		if (params == null) {
			params = Maps.newHashMap();
		}
		params.put(name, value);
		return this;
	}

	/**
	 * Lang.
	 *
	 * @param lang the lang
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder lang(String lang) {
		this.lang = lang;
		return this;
	}

	/**
	 * Interval.
	 *
	 * @param interval the interval
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder interval(String interval) {
		this.interval = interval;
		return this;
	}

	/**
	 * Pre zone adjust large interval.
	 *
	 * @param preZoneAdjustLargeInterval the pre zone adjust large interval
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder preZoneAdjustLargeInterval(boolean preZoneAdjustLargeInterval) {
		this.preZoneAdjustLargeInterval = preZoneAdjustLargeInterval;
		return this;
	}

	/**
	 * Pre zone.
	 *
	 * @param preZone the pre zone
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder preZone(String preZone) {
		this.preZone = preZone;
		return this;
	}

	/**
	 * Post zone.
	 *
	 * @param postZone the post zone
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder postZone(String postZone) {
		this.postZone = postZone;
		return this;
	}

	/**
	 * Pre offset.
	 *
	 * @param preOffset the pre offset
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder preOffset(TimeValue preOffset) {
		this.preOffset = preOffset.millis();
		return this;
	}

	/**
	 * Post offset.
	 *
	 * @param postOffset the post offset
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder postOffset(TimeValue postOffset) {
		this.postOffset = postOffset.millis();
		return this;
	}

	/**
	 * Factor.
	 *
	 * @param factor the factor
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder factor(float factor) {
		this.factor = factor;
		return this;
	}

	/**
	 * Comparator.
	 *
	 * @param comparatorType the comparator type
	 * @return the date histogram facet builder
	 */
	public DateHistogramFacetBuilder comparator(DateHistogramFacet.ComparatorType comparatorType) {
		this.comparatorType = comparatorType;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#global(boolean)
	 */
	@Override
	public DateHistogramFacetBuilder global(boolean global) {
		super.global(global);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#scope(java.lang.String)
	 */
	@Override
	public DateHistogramFacetBuilder scope(String scope) {
		super.scope(scope);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#facetFilter(cn.com.rebirth.search.core.index.query.FilterBuilder)
	 */
	@Override
	public DateHistogramFacetBuilder facetFilter(FilterBuilder filter) {
		this.facetFilter = filter;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#nested(java.lang.String)
	 */
	public DateHistogramFacetBuilder nested(String nested) {
		this.nested = nested;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (keyFieldName == null) {
			throw new SearchSourceBuilderException("field must be set on date histogram facet for facet [" + name + "]");
		}
		if (interval == null) {
			throw new SearchSourceBuilderException("interval must be set on date histogram facet for facet [" + name
					+ "]");
		}
		builder.startObject(name);

		builder.startObject(DateHistogramFacet.TYPE);
		if (valueFieldName != null) {
			builder.field("key_field", keyFieldName);
			builder.field("value_field", valueFieldName);
		} else {
			builder.field("field", keyFieldName);
		}
		if (valueScript != null) {
			builder.field("value_script", valueScript);
			if (lang != null) {
				builder.field("lang", lang);
			}
			if (this.params != null) {
				builder.field("params", this.params);
			}
		}
		builder.field("interval", interval);
		if (preZone != null) {
			builder.field("pre_zone", preZone);
		}
		if (preZoneAdjustLargeInterval != null) {
			builder.field("pre_zone_adjust_large_interval", preZoneAdjustLargeInterval);
		}
		if (postZone != null) {
			builder.field("post_zone", postZone);
		}
		if (preOffset != 0) {
			builder.field("pre_offset", preOffset);
		}
		if (postOffset != 0) {
			builder.field("post_offset", postOffset);
		}
		if (factor != 1.0f) {
			builder.field("factor", factor);
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
