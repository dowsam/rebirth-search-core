/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoDistanceFilterBuilder.java 2012-7-6 14:29:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.commons.unit.DistanceUnit;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.index.search.geo.GeoDistance;

/**
 * The Class GeoDistanceFilterBuilder.
 *
 * @author l.xue.nong
 */
public class GeoDistanceFilterBuilder extends BaseFilterBuilder {

	/** The name. */
	private final String name;

	/** The distance. */
	private String distance;

	/** The lat. */
	private double lat;

	/** The lon. */
	private double lon;

	/** The geohash. */
	private String geohash;

	/** The geo distance. */
	private GeoDistance geoDistance;

	/** The optimize bbox. */
	private String optimizeBbox;

	/** The cache. */
	private Boolean cache;

	/** The cache key. */
	private String cacheKey;

	/** The filter name. */
	private String filterName;

	/**
	 * Instantiates a new geo distance filter builder.
	 *
	 * @param name the name
	 */
	public GeoDistanceFilterBuilder(String name) {
		this.name = name;
	}

	/**
	 * Point.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the geo distance filter builder
	 */
	public GeoDistanceFilterBuilder point(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		return this;
	}

	/**
	 * Lat.
	 *
	 * @param lat the lat
	 * @return the geo distance filter builder
	 */
	public GeoDistanceFilterBuilder lat(double lat) {
		this.lat = lat;
		return this;
	}

	/**
	 * Lon.
	 *
	 * @param lon the lon
	 * @return the geo distance filter builder
	 */
	public GeoDistanceFilterBuilder lon(double lon) {
		this.lon = lon;
		return this;
	}

	/**
	 * Distance.
	 *
	 * @param distance the distance
	 * @return the geo distance filter builder
	 */
	public GeoDistanceFilterBuilder distance(String distance) {
		this.distance = distance;
		return this;
	}

	/**
	 * Distance.
	 *
	 * @param distance the distance
	 * @param unit the unit
	 * @return the geo distance filter builder
	 */
	public GeoDistanceFilterBuilder distance(double distance, DistanceUnit unit) {
		this.distance = unit.toString(distance);
		return this;
	}

	/**
	 * Geohash.
	 *
	 * @param geohash the geohash
	 * @return the geo distance filter builder
	 */
	public GeoDistanceFilterBuilder geohash(String geohash) {
		this.geohash = geohash;
		return this;
	}

	/**
	 * Geo distance.
	 *
	 * @param geoDistance the geo distance
	 * @return the geo distance filter builder
	 */
	public GeoDistanceFilterBuilder geoDistance(GeoDistance geoDistance) {
		this.geoDistance = geoDistance;
		return this;
	}

	/**
	 * Optimize bbox.
	 *
	 * @param optimizeBbox the optimize bbox
	 * @return the geo distance filter builder
	 */
	public GeoDistanceFilterBuilder optimizeBbox(String optimizeBbox) {
		this.optimizeBbox = optimizeBbox;
		return this;
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the geo distance filter builder
	 */
	public GeoDistanceFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the geo distance filter builder
	 */
	public GeoDistanceFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the geo distance filter builder
	 */
	public GeoDistanceFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(GeoDistanceFilterParser.NAME);
		if (geohash != null) {
			builder.field(name, geohash);
		} else {
			builder.startArray(name).value(lon).value(lat).endArray();
		}
		builder.field("distance", distance);
		if (geoDistance != null) {
			builder.field("distance_type", geoDistance.name().toLowerCase());
		}
		if (optimizeBbox != null) {
			builder.field("optimize_bbox", optimizeBbox);
		}
		if (filterName != null) {
			builder.field("_name", filterName);
		}
		if (cache != null) {
			builder.field("_cache", cache);
		}
		if (cacheKey != null) {
			builder.field("_cache_key", cacheKey);
		}
		builder.endObject();
	}
}
