/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GeoDistanceRangeFilterBuilder.java 2012-3-29 15:02:16 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.index.search.geo.GeoDistance;


/**
 * The Class GeoDistanceRangeFilterBuilder.
 *
 * @author l.xue.nong
 */
public class GeoDistanceRangeFilterBuilder extends BaseFilterBuilder {

	
	/** The name. */
	private final String name;

	
	/** The from. */
	private Object from;

	
	/** The to. */
	private Object to;

	
	/** The include lower. */
	private boolean includeLower = true;

	
	/** The include upper. */
	private boolean includeUpper = true;

	
	/** The lat. */
	private double lat;

	
	/** The lon. */
	private double lon;

	
	/** The geohash. */
	private String geohash;

	
	/** The geo distance. */
	private GeoDistance geoDistance;

	
	/** The cache. */
	private Boolean cache;

	
	/** The cache key. */
	private String cacheKey;

	
	/** The filter name. */
	private String filterName;

	
	/** The optimize bbox. */
	private String optimizeBbox;

	
	/**
	 * Instantiates a new geo distance range filter builder.
	 *
	 * @param name the name
	 */
	public GeoDistanceRangeFilterBuilder(String name) {
		this.name = name;
	}

	
	/**
	 * Point.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder point(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		return this;
	}

	
	/**
	 * Lat.
	 *
	 * @param lat the lat
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder lat(double lat) {
		this.lat = lat;
		return this;
	}

	
	/**
	 * Lon.
	 *
	 * @param lon the lon
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder lon(double lon) {
		this.lon = lon;
		return this;
	}

	
	/**
	 * From.
	 *
	 * @param from the from
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder from(Object from) {
		this.from = from;
		return this;
	}

	
	/**
	 * To.
	 *
	 * @param to the to
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder to(Object to) {
		this.to = to;
		return this;
	}

	
	/**
	 * Gt.
	 *
	 * @param from the from
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder gt(Object from) {
		this.from = from;
		this.includeLower = false;
		return this;
	}

	
	/**
	 * Gte.
	 *
	 * @param from the from
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder gte(Object from) {
		this.from = from;
		this.includeLower = true;
		return this;
	}

	
	/**
	 * Lt.
	 *
	 * @param to the to
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder lt(Object to) {
		this.to = to;
		this.includeUpper = false;
		return this;
	}

	
	/**
	 * Lte.
	 *
	 * @param to the to
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder lte(Object to) {
		this.to = to;
		this.includeUpper = true;
		return this;
	}

	
	/**
	 * Include lower.
	 *
	 * @param includeLower the include lower
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder includeLower(boolean includeLower) {
		this.includeLower = includeLower;
		return this;
	}

	
	/**
	 * Include upper.
	 *
	 * @param includeUpper the include upper
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder includeUpper(boolean includeUpper) {
		this.includeUpper = includeUpper;
		return this;
	}

	
	/**
	 * Geohash.
	 *
	 * @param geohash the geohash
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder geohash(String geohash) {
		this.geohash = geohash;
		return this;
	}

	
	/**
	 * Geo distance.
	 *
	 * @param geoDistance the geo distance
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder geoDistance(GeoDistance geoDistance) {
		this.geoDistance = geoDistance;
		return this;
	}

	
	/**
	 * Optimize bbox.
	 *
	 * @param optimizeBbox the optimize bbox
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder optimizeBbox(String optimizeBbox) {
		this.optimizeBbox = optimizeBbox;
		return this;
	}

	
	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	
	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	
	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the geo distance range filter builder
	 */
	public GeoDistanceRangeFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(GeoDistanceRangeFilterParser.NAME);
		if (geohash != null) {
			builder.field(name, geohash);
		} else {
			builder.startArray(name).value(lon).value(lat).endArray();
		}
		builder.field("from", from);
		builder.field("to", to);
		builder.field("include_lower", includeLower);
		builder.field("include_upper", includeUpper);
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
