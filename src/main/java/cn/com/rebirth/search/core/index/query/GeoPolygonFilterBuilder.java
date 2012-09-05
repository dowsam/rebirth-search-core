/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoPolygonFilterBuilder.java 2012-7-6 14:30:01 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.List;

import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.index.search.geo.GeoHashUtils;
import cn.com.rebirth.search.core.index.search.geo.Point;

import com.google.common.collect.Lists;

/**
 * The Class GeoPolygonFilterBuilder.
 *
 * @author l.xue.nong
 */
public class GeoPolygonFilterBuilder extends BaseFilterBuilder {

	/** The name. */
	private final String name;

	/** The points. */
	private final List<Point> points = Lists.newArrayList();

	/** The cache. */
	private Boolean cache;

	/** The cache key. */
	private String cacheKey;

	/** The filter name. */
	private String filterName;

	/**
	 * Instantiates a new geo polygon filter builder.
	 *
	 * @param name the name
	 */
	public GeoPolygonFilterBuilder(String name) {
		this.name = name;
	}

	/**
	 * Adds the point.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the geo polygon filter builder
	 */
	public GeoPolygonFilterBuilder addPoint(double lat, double lon) {
		points.add(new Point(lat, lon));
		return this;
	}

	/**
	 * Adds the point.
	 *
	 * @param geohash the geohash
	 * @return the geo polygon filter builder
	 */
	public GeoPolygonFilterBuilder addPoint(String geohash) {
		double[] values = GeoHashUtils.decode(geohash);
		return addPoint(values[0], values[1]);
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the geo polygon filter builder
	 */
	public GeoPolygonFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the geo polygon filter builder
	 */
	public GeoPolygonFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the geo polygon filter builder
	 */
	public GeoPolygonFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(GeoPolygonFilterParser.NAME);

		builder.startObject(name);
		builder.startArray("points");
		for (Point point : points) {
			builder.startArray().value(point.lon).value(point.lat).endArray();
		}
		builder.endArray();
		builder.endObject();

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
