/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoBoundingBoxFilterBuilder.java 2012-7-6 14:29:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.index.search.geo.Point;

/**
 * The Class GeoBoundingBoxFilterBuilder.
 *
 * @author l.xue.nong
 */
public class GeoBoundingBoxFilterBuilder extends BaseFilterBuilder {

	/** The name. */
	private final String name;

	/** The top left. */
	private Point topLeft;

	/** The top left geohash. */
	private String topLeftGeohash;

	/** The bottom right. */
	private Point bottomRight;

	/** The bottom right geohash. */
	private String bottomRightGeohash;

	/** The cache. */
	private Boolean cache;

	/** The cache key. */
	private String cacheKey;

	/** The filter name. */
	private String filterName;

	/** The type. */
	private String type;

	/**
	 * Instantiates a new geo bounding box filter builder.
	 *
	 * @param name the name
	 */
	public GeoBoundingBoxFilterBuilder(String name) {
		this.name = name;
	}

	/**
	 * Top left.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the geo bounding box filter builder
	 */
	public GeoBoundingBoxFilterBuilder topLeft(double lat, double lon) {
		topLeft = new Point();
		topLeft.lat = lat;
		topLeft.lon = lon;
		return this;
	}

	/**
	 * Bottom right.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the geo bounding box filter builder
	 */
	public GeoBoundingBoxFilterBuilder bottomRight(double lat, double lon) {
		bottomRight = new Point();
		bottomRight.lat = lat;
		bottomRight.lon = lon;
		return this;
	}

	/**
	 * Top left.
	 *
	 * @param geohash the geohash
	 * @return the geo bounding box filter builder
	 */
	public GeoBoundingBoxFilterBuilder topLeft(String geohash) {
		this.topLeftGeohash = geohash;
		return this;
	}

	/**
	 * Bottom right.
	 *
	 * @param geohash the geohash
	 * @return the geo bounding box filter builder
	 */
	public GeoBoundingBoxFilterBuilder bottomRight(String geohash) {
		this.bottomRightGeohash = geohash;
		return this;
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the geo bounding box filter builder
	 */
	public GeoBoundingBoxFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the geo bounding box filter builder
	 */
	public GeoBoundingBoxFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the geo bounding box filter builder
	 */
	public GeoBoundingBoxFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	/**
	 * Type.
	 *
	 * @param type the type
	 * @return the geo bounding box filter builder
	 */
	public GeoBoundingBoxFilterBuilder type(String type) {
		this.type = type;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(GeoBoundingBoxFilterParser.NAME);

		builder.startObject(name);
		if (topLeftGeohash != null) {
			builder.field("top_left", topLeftGeohash);
		} else if (topLeft != null) {
			builder.startArray("top_left").value(topLeft.lon).value(topLeft.lat).endArray();
		} else {
			throw new QueryBuilderException("geo_bounding_box requires 'top_left' to be set");
		}

		if (bottomRightGeohash != null) {
			builder.field("bottom_right", bottomRightGeohash);
		} else if (bottomRight != null) {
			builder.startArray("bottom_right").value(bottomRight.lon).value(bottomRight.lat).endArray();
		} else {
			throw new QueryBuilderException("geo_bounding_box requires 'bottom_right' to be set");
		}
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
		if (type != null) {
			builder.field("type", type);
		}

		builder.endObject();
	}
}
