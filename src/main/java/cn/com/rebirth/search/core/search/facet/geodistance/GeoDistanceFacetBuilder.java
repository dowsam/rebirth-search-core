/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoDistanceFacetBuilder.java 2012-7-6 14:29:38 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.geodistance;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.unit.DistanceUnit;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.index.search.geo.GeoDistance;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilderException;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The Class GeoDistanceFacetBuilder.
 *
 * @author l.xue.nong
 */
public class GeoDistanceFacetBuilder extends AbstractFacetBuilder {

	/** The field name. */
	private String fieldName;

	/** The value field name. */
	private String valueFieldName;

	/** The lat. */
	private double lat;

	/** The lon. */
	private double lon;

	/** The geohash. */
	private String geohash;

	/** The geo distance. */
	private GeoDistance geoDistance;

	/** The unit. */
	private DistanceUnit unit;

	/** The params. */
	private Map<String, Object> params;

	/** The value script. */
	private String valueScript;

	/** The lang. */
	private String lang;

	/** The entries. */
	private List<Entry> entries = Lists.newArrayList();

	/**
	 * Instantiates a new geo distance facet builder.
	 *
	 * @param name the name
	 */
	public GeoDistanceFacetBuilder(String name) {
		super(name);
	}

	/**
	 * Field.
	 *
	 * @param fieldName the field name
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder field(String fieldName) {
		this.fieldName = fieldName;
		return this;
	}

	/**
	 * Value field.
	 *
	 * @param valueFieldName the value field name
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder valueField(String valueFieldName) {
		this.valueFieldName = valueFieldName;
		return this;
	}

	/**
	 * Value script.
	 *
	 * @param valueScript the value script
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder valueScript(String valueScript) {
		this.valueScript = valueScript;
		return this;
	}

	/**
	 * Lang.
	 *
	 * @param lang the lang
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder lang(String lang) {
		this.lang = lang;
		return this;
	}

	/**
	 * Script param.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder scriptParam(String name, Object value) {
		if (params == null) {
			params = Maps.newHashMap();
		}
		params.put(name, value);
		return this;
	}

	/**
	 * Point.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder point(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		return this;
	}

	/**
	 * Lat.
	 *
	 * @param lat the lat
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder lat(double lat) {
		this.lat = lat;
		return this;
	}

	/**
	 * Lon.
	 *
	 * @param lon the lon
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder lon(double lon) {
		this.lon = lon;
		return this;
	}

	/**
	 * Geohash.
	 *
	 * @param geohash the geohash
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder geohash(String geohash) {
		this.geohash = geohash;
		return this;
	}

	/**
	 * Geo distance.
	 *
	 * @param geoDistance the geo distance
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder geoDistance(GeoDistance geoDistance) {
		this.geoDistance = geoDistance;
		return this;
	}

	/**
	 * Adds the range.
	 *
	 * @param from the from
	 * @param to the to
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder addRange(double from, double to) {
		entries.add(new Entry(from, to));
		return this;
	}

	/**
	 * Adds the unbounded to.
	 *
	 * @param from the from
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder addUnboundedTo(double from) {
		entries.add(new Entry(from, Double.POSITIVE_INFINITY));
		return this;
	}

	/**
	 * Adds the unbounded from.
	 *
	 * @param to the to
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder addUnboundedFrom(double to) {
		entries.add(new Entry(Double.NEGATIVE_INFINITY, to));
		return this;
	}

	/**
	 * Unit.
	 *
	 * @param unit the unit
	 * @return the geo distance facet builder
	 */
	public GeoDistanceFacetBuilder unit(DistanceUnit unit) {
		this.unit = unit;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#global(boolean)
	 */
	public GeoDistanceFacetBuilder global(boolean global) {
		super.global(global);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#scope(java.lang.String)
	 */
	@Override
	public GeoDistanceFacetBuilder scope(String scope) {
		super.scope(scope);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#facetFilter(cn.com.rebirth.search.core.index.query.FilterBuilder)
	 */
	public GeoDistanceFacetBuilder facetFilter(FilterBuilder filter) {
		this.facetFilter = filter;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder#nested(java.lang.String)
	 */
	public GeoDistanceFacetBuilder nested(String nested) {
		this.nested = nested;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (fieldName == null) {
			throw new SearchSourceBuilderException("field must be set on geo_distance facet for facet [" + name + "]");
		}
		if (entries.isEmpty()) {
			throw new SearchSourceBuilderException("at least one range must be defined for geo_distance facet [" + name
					+ "]");
		}

		builder.startObject(name);

		builder.startObject(GeoDistanceFacet.TYPE);

		if (geohash != null) {
			builder.field(fieldName, geohash);
		} else {
			builder.startArray(fieldName).value(lon).value(lat).endArray();
		}

		if (valueFieldName != null) {
			builder.field("value_field", valueFieldName);
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

		if (unit != null) {
			builder.field("unit", unit);
		}
		if (geoDistance != null) {
			builder.field("distance_type", geoDistance.name().toLowerCase());
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
