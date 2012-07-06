/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoDistanceDataComparator.java 2012-7-6 14:29:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search.geo;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.SortField;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.unit.DistanceUnit;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldData;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldDataType;
import cn.com.rebirth.search.index.field.data.ExtendedFieldComparatorSource;

/**
 * The Class GeoDistanceDataComparator.
 *
 * @author l.xue.nong
 */
public class GeoDistanceDataComparator extends FieldComparator {

	/**
	 * Comparator source.
	 *
	 * @param fieldName the field name
	 * @param lat the lat
	 * @param lon the lon
	 * @param unit the unit
	 * @param geoDistance the geo distance
	 * @param fieldDataCache the field data cache
	 * @param mapperService the mapper service
	 * @return the extended field comparator source
	 */
	public static ExtendedFieldComparatorSource comparatorSource(String fieldName, double lat, double lon,
			DistanceUnit unit, GeoDistance geoDistance, FieldDataCache fieldDataCache, MapperService mapperService) {
		return new InnerSource(fieldName, lat, lon, unit, geoDistance, fieldDataCache, mapperService);
	}

	/**
	 * The Class InnerSource.
	 *
	 * @author l.xue.nong
	 */
	static class InnerSource extends ExtendedFieldComparatorSource {

		/** The field name. */
		protected final String fieldName;

		/** The lat. */
		protected final double lat;

		/** The lon. */
		protected final double lon;

		/** The unit. */
		protected final DistanceUnit unit;

		/** The geo distance. */
		protected final GeoDistance geoDistance;

		/** The field data cache. */
		protected final FieldDataCache fieldDataCache;

		/** The mapper service. */
		private final MapperService mapperService;

		/**
		 * Instantiates a new inner source.
		 *
		 * @param fieldName the field name
		 * @param lat the lat
		 * @param lon the lon
		 * @param unit the unit
		 * @param geoDistance the geo distance
		 * @param fieldDataCache the field data cache
		 * @param mapperService the mapper service
		 */
		private InnerSource(String fieldName, double lat, double lon, DistanceUnit unit, GeoDistance geoDistance,
				FieldDataCache fieldDataCache, MapperService mapperService) {
			this.fieldName = fieldName;
			this.lat = lat;
			this.lon = lon;
			this.unit = unit;
			this.geoDistance = geoDistance;
			this.fieldDataCache = fieldDataCache;
			this.mapperService = mapperService;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.FieldComparatorSource#newComparator(java.lang.String, int, int, boolean)
		 */
		@Override
		public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
				throws IOException {
			return new GeoDistanceDataComparator(numHits, fieldname, lat, lon, unit, geoDistance, fieldDataCache,
					mapperService);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.index.field.data.ExtendedFieldComparatorSource#reducedType()
		 */
		@Override
		public int reducedType() {
			return SortField.DOUBLE;
		}
	}

	/** The field name. */
	protected final String fieldName;

	/** The index field name. */
	protected final String indexFieldName;

	/** The lat. */
	protected final double lat;

	/** The lon. */
	protected final double lon;

	/** The unit. */
	protected final DistanceUnit unit;

	/** The geo distance. */
	protected final GeoDistance geoDistance;

	/** The fixed source distance. */
	protected final GeoDistance.FixedSourceDistance fixedSourceDistance;

	/** The field data cache. */
	protected final FieldDataCache fieldDataCache;

	/** The field data. */
	protected GeoPointFieldData fieldData;

	/** The values. */
	private final double[] values;

	/** The bottom. */
	private double bottom;

	/**
	 * Instantiates a new geo distance data comparator.
	 *
	 * @param numHits the num hits
	 * @param fieldName the field name
	 * @param lat the lat
	 * @param lon the lon
	 * @param unit the unit
	 * @param geoDistance the geo distance
	 * @param fieldDataCache the field data cache
	 * @param mapperService the mapper service
	 */
	public GeoDistanceDataComparator(int numHits, String fieldName, double lat, double lon, DistanceUnit unit,
			GeoDistance geoDistance, FieldDataCache fieldDataCache, MapperService mapperService) {
		values = new double[numHits];

		this.fieldName = fieldName;
		this.lat = lat;
		this.lon = lon;
		this.unit = unit;
		this.geoDistance = geoDistance;
		this.fieldDataCache = fieldDataCache;

		this.fixedSourceDistance = geoDistance.fixedSourceDistance(lat, lon, unit);

		FieldMapper mapper = mapperService.smartNameFieldMapper(fieldName);
		if (mapper == null) {
			throw new RebirthIllegalArgumentException("No mapping found for field [" + fieldName
					+ "] for geo distance sort");
		}
		if (mapper.fieldDataType() != GeoPointFieldDataType.TYPE) {
			throw new RebirthIllegalArgumentException("field [" + fieldName + "] is not a geo_point field");
		}
		this.indexFieldName = mapper.names().indexName();
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#setNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		fieldData = (GeoPointFieldData) fieldDataCache.cache(GeoPointFieldDataType.TYPE, reader, indexFieldName);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#compare(int, int)
	 */
	@Override
	public int compare(int slot1, int slot2) {
		final double v1 = values[slot1];
		final double v2 = values[slot2];
		if (v1 > v2) {
			return 1;
		} else if (v1 < v2) {
			return -1;
		} else {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#compareBottom(int)
	 */
	@Override
	public int compareBottom(int doc) {
		double distance;
		if (!fieldData.hasValue(doc)) {

			distance = Double.MAX_VALUE;
		} else {
			distance = fixedSourceDistance.calculate(fieldData.latValue(doc), fieldData.lonValue(doc));
		}
		final double v2 = distance;
		if (bottom > v2) {
			return 1;
		} else if (bottom < v2) {
			return -1;
		} else {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#copy(int, int)
	 */
	@Override
	public void copy(int slot, int doc) {
		double distance;
		if (!fieldData.hasValue(doc)) {

			distance = Double.MAX_VALUE;
		} else {
			distance = fixedSourceDistance.calculate(fieldData.latValue(doc), fieldData.lonValue(doc));
		}
		values[slot] = distance;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#setBottom(int)
	 */
	@Override
	public void setBottom(final int bottom) {
		this.bottom = values[bottom];
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#value(int)
	 */
	@Override
	public Comparable value(int slot) {
		return Double.valueOf(values[slot]);
	}
}
