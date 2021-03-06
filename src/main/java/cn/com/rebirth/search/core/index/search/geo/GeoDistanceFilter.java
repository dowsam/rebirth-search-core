/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoDistanceFilter.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search.geo;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.unit.DistanceUnit;
import cn.com.rebirth.search.commons.lucene.docset.AndDocSet;
import cn.com.rebirth.search.commons.lucene.docset.DocSet;
import cn.com.rebirth.search.commons.lucene.docset.DocSets;
import cn.com.rebirth.search.commons.lucene.docset.GetDocSet;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldData;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldDataType;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldMapper;

import com.google.common.collect.ImmutableList;

/**
 * The Class GeoDistanceFilter.
 *
 * @author l.xue.nong
 */
public class GeoDistanceFilter extends Filter {

	/** The lat. */
	private final double lat;

	/** The lon. */
	private final double lon;

	/** The distance. */
	private final double distance;

	/** The geo distance. */
	private final GeoDistance geoDistance;

	/** The field name. */
	private final String fieldName;

	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	/** The fixed source distance. */
	private final GeoDistance.FixedSourceDistance fixedSourceDistance;

	/** The distance bounding check. */
	private GeoDistance.DistanceBoundingCheck distanceBoundingCheck;

	/** The bounding box filter. */
	private final Filter boundingBoxFilter;

	/**
	 * Instantiates a new geo distance filter.
	 *
	 * @param lat the lat
	 * @param lon the lon
	 * @param distance the distance
	 * @param geoDistance the geo distance
	 * @param fieldName the field name
	 * @param mapper the mapper
	 * @param fieldDataCache the field data cache
	 * @param optimizeBbox the optimize bbox
	 */
	public GeoDistanceFilter(double lat, double lon, double distance, GeoDistance geoDistance, String fieldName,
			GeoPointFieldMapper mapper, FieldDataCache fieldDataCache, String optimizeBbox) {
		this.lat = lat;
		this.lon = lon;
		this.distance = distance;
		this.geoDistance = geoDistance;
		this.fieldName = fieldName;
		this.fieldDataCache = fieldDataCache;

		this.fixedSourceDistance = geoDistance.fixedSourceDistance(lat, lon, DistanceUnit.MILES);
		if (optimizeBbox != null && !"none".equals(optimizeBbox)) {
			distanceBoundingCheck = GeoDistance.distanceBoundingCheck(lat, lon, distance, DistanceUnit.MILES);
			if ("memory".equals(optimizeBbox)) {
				boundingBoxFilter = null;
			} else if ("indexed".equals(optimizeBbox)) {
				boundingBoxFilter = IndexedGeoBoundingBoxFilter.create(distanceBoundingCheck.topLeft(),
						distanceBoundingCheck.bottomRight(), mapper);
				distanceBoundingCheck = GeoDistance.ALWAYS_INSTANCE;
			} else {
				throw new RebirthIllegalArgumentException("type [" + optimizeBbox
						+ "] for bounding box optimization not supported");
			}
		} else {
			distanceBoundingCheck = GeoDistance.ALWAYS_INSTANCE;
			boundingBoxFilter = null;
		}
	}

	/**
	 * Lat.
	 *
	 * @return the double
	 */
	public double lat() {
		return lat;
	}

	/**
	 * Lon.
	 *
	 * @return the double
	 */
	public double lon() {
		return lon;
	}

	/**
	 * Distance.
	 *
	 * @return the double
	 */
	public double distance() {
		return distance;
	}

	/**
	 * Geo distance.
	 *
	 * @return the geo distance
	 */
	public GeoDistance geoDistance() {
		return geoDistance;
	}

	/**
	 * Field name.
	 *
	 * @return the string
	 */
	public String fieldName() {
		return fieldName;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
		DocSet boundingBoxDocSet = null;
		if (boundingBoxFilter != null) {
			DocIdSet docIdSet = boundingBoxFilter.getDocIdSet(reader);
			if (docIdSet == null) {
				return null;
			}
			boundingBoxDocSet = DocSets.convert(reader, docIdSet);
		}
		final GeoPointFieldData fieldData = (GeoPointFieldData) fieldDataCache.cache(GeoPointFieldDataType.TYPE,
				reader, fieldName);
		GeoDistanceDocSet distDocSet = new GeoDistanceDocSet(reader.maxDoc(), fieldData, fixedSourceDistance,
				distanceBoundingCheck, distance);
		if (boundingBoxDocSet == null) {
			return distDocSet;
		} else {
			return new AndDocSet(ImmutableList.of(boundingBoxDocSet, distDocSet));
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		GeoDistanceFilter filter = (GeoDistanceFilter) o;

		if (Double.compare(filter.distance, distance) != 0)
			return false;
		if (Double.compare(filter.lat, lat) != 0)
			return false;
		if (Double.compare(filter.lon, lon) != 0)
			return false;
		if (fieldName != null ? !fieldName.equals(filter.fieldName) : filter.fieldName != null)
			return false;
		if (geoDistance != filter.geoDistance)
			return false;

		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = lat != +0.0d ? Double.doubleToLongBits(lat) : 0L;
		result = (int) (temp ^ (temp >>> 32));
		temp = lon != +0.0d ? Double.doubleToLongBits(lon) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = distance != +0.0d ? Double.doubleToLongBits(distance) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (geoDistance != null ? geoDistance.hashCode() : 0);
		result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
		return result;
	}

	/**
	 * The Class GeoDistanceDocSet.
	 *
	 * @author l.xue.nong
	 */
	public static class GeoDistanceDocSet extends GetDocSet {

		/** The distance. */
		private final double distance;

		/** The field data. */
		private final GeoPointFieldData fieldData;

		/** The fixed source distance. */
		private final GeoDistance.FixedSourceDistance fixedSourceDistance;

		/** The distance bounding check. */
		private final GeoDistance.DistanceBoundingCheck distanceBoundingCheck;

		/**
		 * Instantiates a new geo distance doc set.
		 *
		 * @param maxDoc the max doc
		 * @param fieldData the field data
		 * @param fixedSourceDistance the fixed source distance
		 * @param distanceBoundingCheck the distance bounding check
		 * @param distance the distance
		 */
		public GeoDistanceDocSet(int maxDoc, GeoPointFieldData fieldData,
				GeoDistance.FixedSourceDistance fixedSourceDistance,
				GeoDistance.DistanceBoundingCheck distanceBoundingCheck, double distance) {
			super(maxDoc);
			this.fieldData = fieldData;
			this.fixedSourceDistance = fixedSourceDistance;
			this.distanceBoundingCheck = distanceBoundingCheck;
			this.distance = distance;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.DocIdSet#isCacheable()
		 */
		@Override
		public boolean isCacheable() {

			return false;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.commons.lucene.docset.DocSet#get(int)
		 */
		@Override
		public boolean get(int doc) {
			if (!fieldData.hasValue(doc)) {
				return false;
			}

			if (fieldData.multiValued()) {
				double[] lats = fieldData.latValues(doc);
				double[] lons = fieldData.lonValues(doc);
				for (int i = 0; i < lats.length; i++) {
					double lat = lats[i];
					double lon = lons[i];
					if (distanceBoundingCheck.isWithin(lat, lon)) {
						double d = fixedSourceDistance.calculate(lat, lon);
						if (d < distance) {
							return true;
						}
					}
				}
				return false;
			} else {
				double lat = fieldData.latValue(doc);
				double lon = fieldData.lonValue(doc);
				if (distanceBoundingCheck.isWithin(lat, lon)) {
					double d = fixedSourceDistance.calculate(lat, lon);
					return d < distance;
				}
			}
			return false;
		}
	}
}
