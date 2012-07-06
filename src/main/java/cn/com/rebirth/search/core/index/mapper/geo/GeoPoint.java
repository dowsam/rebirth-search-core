/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GeoPoint.java 2012-3-29 15:02:47 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.geo;

import cn.com.rebirth.search.core.index.search.geo.GeoHashUtils;


/**
 * The Class GeoPoint.
 *
 * @author l.xue.nong
 */
public class GeoPoint {

	
	/** The lat. */
	private double lat;

	
	/** The lon. */
	private double lon;

	
	/**
	 * Instantiates a new geo point.
	 */
	GeoPoint() {
	}

	
	/**
	 * Instantiates a new geo point.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 */
	public GeoPoint(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	
	/**
	 * Latlon.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 */
	void latlon(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	
	/**
	 * Lat.
	 *
	 * @return the double
	 */
	public final double lat() {
		return this.lat;
	}

	
	/**
	 * Gets the lat.
	 *
	 * @return the lat
	 */
	public final double getLat() {
		return this.lat;
	}

	
	/**
	 * Lon.
	 *
	 * @return the double
	 */
	public final double lon() {
		return this.lon;
	}

	
	/**
	 * Gets the lon.
	 *
	 * @return the lon
	 */
	public final double getLon() {
		return this.lon;
	}

	
	/**
	 * Geohash.
	 *
	 * @return the string
	 */
	public final String geohash() {
		return GeoHashUtils.encode(lat, lon);
	}

	
	/**
	 * Gets the geohash.
	 *
	 * @return the geohash
	 */
	public final String getGeohash() {
		return GeoHashUtils.encode(lat, lon);
	}
}
