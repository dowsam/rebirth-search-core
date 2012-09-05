/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoDistanceSortBuilder.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.sort;

import java.io.IOException;

import cn.com.rebirth.commons.unit.DistanceUnit;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.index.search.geo.GeoDistance;

/**
 * The Class GeoDistanceSortBuilder.
 *
 * @author l.xue.nong
 */
public class GeoDistanceSortBuilder extends SortBuilder {

	/** The field name. */
	final String fieldName;

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

	/** The order. */
	private SortOrder order;

	/**
	 * Instantiates a new geo distance sort builder.
	 *
	 * @param fieldName the field name
	 */
	public GeoDistanceSortBuilder(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * Point.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the geo distance sort builder
	 */
	public GeoDistanceSortBuilder point(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		return this;
	}

	/**
	 * Geohash.
	 *
	 * @param geohash the geohash
	 * @return the geo distance sort builder
	 */
	public GeoDistanceSortBuilder geohash(String geohash) {
		this.geohash = geohash;
		return this;
	}

	/**
	 * Geo distance.
	 *
	 * @param geoDistance the geo distance
	 * @return the geo distance sort builder
	 */
	public GeoDistanceSortBuilder geoDistance(GeoDistance geoDistance) {
		this.geoDistance = geoDistance;
		return this;
	}

	/**
	 * Unit.
	 *
	 * @param unit the unit
	 * @return the geo distance sort builder
	 */
	public GeoDistanceSortBuilder unit(DistanceUnit unit) {
		this.unit = unit;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.sort.SortBuilder#order(cn.com.rebirth.search.core.search.sort.SortOrder)
	 */
	@Override
	public GeoDistanceSortBuilder order(SortOrder order) {
		this.order = order;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.sort.SortBuilder#missing(java.lang.Object)
	 */
	@Override
	public SortBuilder missing(Object missing) {
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject("_geo_distance");

		if (geohash != null) {
			builder.field(fieldName, geohash);
		} else {
			builder.startArray(fieldName).value(lon).value(lat).endArray();
		}

		if (unit != null) {
			builder.field("unit", unit);
		}
		if (geoDistance != null) {
			builder.field("distance_type", geoDistance.name().toLowerCase());
		}
		if (order == SortOrder.DESC) {
			builder.field("reverse", true);
		}

		builder.endObject();
		return builder;
	}
}
