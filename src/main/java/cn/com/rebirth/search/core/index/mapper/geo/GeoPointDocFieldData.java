/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GeoPointDocFieldData.java 2012-3-29 15:01:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.geo;

import cn.com.rebirth.commons.unit.DistanceUnit;
import cn.com.rebirth.search.core.index.field.data.DocFieldData;


/**
 * The Class GeoPointDocFieldData.
 *
 * @author l.xue.nong
 */
public class GeoPointDocFieldData extends DocFieldData<GeoPointFieldData> {

	
	/**
	 * Instantiates a new geo point doc field data.
	 *
	 * @param fieldData the field data
	 */
	public GeoPointDocFieldData(GeoPointFieldData fieldData) {
		super(fieldData);
	}

	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public GeoPoint getValue() {
		return fieldData.value(docId);
	}

	
	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public GeoPoint[] getValues() {
		return fieldData.values(docId);
	}

	
	/**
	 * Factor distance.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the double
	 */
	public double factorDistance(double lat, double lon) {
		return fieldData.factorDistance(docId, DistanceUnit.MILES, lat, lon);
	}

	
	/**
	 * Factor distance02.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the double
	 */
	public double factorDistance02(double lat, double lon) {
		return fieldData.factorDistance(docId, DistanceUnit.MILES, lat, lon) + 1;
	}

	
	/**
	 * Factor distance13.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the double
	 */
	public double factorDistance13(double lat, double lon) {
		return fieldData.factorDistance(docId, DistanceUnit.MILES, lat, lon) + 2;
	}

	
	/**
	 * Arc distance.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the double
	 */
	public double arcDistance(double lat, double lon) {
		return fieldData.arcDistance(docId, DistanceUnit.MILES, lat, lon);
	}

	
	/**
	 * Arc distance in km.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the double
	 */
	public double arcDistanceInKm(double lat, double lon) {
		return fieldData.arcDistance(docId, DistanceUnit.KILOMETERS, lat, lon);
	}

	
	/**
	 * Distance.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the double
	 */
	public double distance(double lat, double lon) {
		return fieldData.distance(docId, DistanceUnit.MILES, lat, lon);
	}

	
	/**
	 * Distance in km.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @return the double
	 */
	public double distanceInKm(double lat, double lon) {
		return fieldData.distance(docId, DistanceUnit.KILOMETERS, lat, lon);
	}

	
	/**
	 * Geohash distance.
	 *
	 * @param geohash the geohash
	 * @return the double
	 */
	public double geohashDistance(String geohash) {
		return fieldData.distanceGeohash(docId, DistanceUnit.MILES, geohash);
	}

	
	/**
	 * Geohash distance in km.
	 *
	 * @param geohash the geohash
	 * @return the double
	 */
	public double geohashDistanceInKm(String geohash) {
		return fieldData.distanceGeohash(docId, DistanceUnit.KILOMETERS, geohash);
	}

	
	/**
	 * Gets the lat.
	 *
	 * @return the lat
	 */
	public double getLat() {
		return fieldData.latValue(docId);
	}

	
	/**
	 * Gets the lon.
	 *
	 * @return the lon
	 */
	public double getLon() {
		return fieldData.lonValue(docId);
	}

	
	/**
	 * Gets the lats.
	 *
	 * @return the lats
	 */
	public double[] getLats() {
		return fieldData.latValues(docId);
	}

	
	/**
	 * Gets the lons.
	 *
	 * @return the lons
	 */
	public double[] getLons() {
		return fieldData.lonValues(docId);
	}
}
