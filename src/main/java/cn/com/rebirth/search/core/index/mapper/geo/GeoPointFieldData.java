/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoPointFieldData.java 2012-7-6 14:29:16 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.geo;

import gnu.trove.list.array.TDoubleArrayList;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.commons.unit.DistanceUnit;
import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.FieldData;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldData.GeoPointHash;
import cn.com.rebirth.search.core.index.search.geo.GeoDistance;
import cn.com.rebirth.search.core.index.search.geo.GeoHashUtils;

/**
 * The Class GeoPointFieldData.
 *
 * @author l.xue.nong
 */
public abstract class GeoPointFieldData extends FieldData<GeoPointDocFieldData> {

	/** The values cache. */
	static ThreadLocal<ThreadLocals.CleanableValue<GeoPoint>> valuesCache = new ThreadLocal<ThreadLocals.CleanableValue<GeoPoint>>() {
		@Override
		protected ThreadLocals.CleanableValue<GeoPoint> initialValue() {
			return new ThreadLocals.CleanableValue<GeoPoint>(new GeoPoint());
		}
	};

	/**
	 * The Class GeoPointHash.
	 *
	 * @author l.xue.nong
	 */
	static class GeoPointHash {

		/** The lat. */
		public double lat;

		/** The lon. */
		public double lon;

		/** The geo hash. */
		public String geoHash = "";
	}

	/** The geo hash cache. */
	static ThreadLocal<ThreadLocals.CleanableValue<GeoPointHash>> geoHashCache = new ThreadLocal<ThreadLocals.CleanableValue<GeoPointHash>>() {
		@Override
		protected ThreadLocals.CleanableValue<GeoPointHash> initialValue() {
			return new ThreadLocals.CleanableValue<GeoPointHash>(new GeoPointHash());
		}
	};

	/** The Constant EMPTY_ARRAY. */
	public static final GeoPoint[] EMPTY_ARRAY = new GeoPoint[0];

	/** The lat. */
	protected final double[] lat;

	/** The lon. */
	protected final double[] lon;

	/**
	 * Instantiates a new geo point field data.
	 *
	 * @param fieldName the field name
	 * @param lat the lat
	 * @param lon the lon
	 */
	protected GeoPointFieldData(String fieldName, double[] lat, double[] lon) {
		super(fieldName);
		this.lat = lat;
		this.lon = lon;
	}

	/**
	 * Value.
	 *
	 * @param docId the doc id
	 * @return the geo point
	 */
	abstract public GeoPoint value(int docId);

	/**
	 * Values.
	 *
	 * @param docId the doc id
	 * @return the geo point[]
	 */
	abstract public GeoPoint[] values(int docId);

	/**
	 * Lat value.
	 *
	 * @param docId the doc id
	 * @return the double
	 */
	abstract public double latValue(int docId);

	/**
	 * Lon value.
	 *
	 * @param docId the doc id
	 * @return the double
	 */
	abstract public double lonValue(int docId);

	/**
	 * Lat values.
	 *
	 * @param docId the doc id
	 * @return the double[]
	 */
	abstract public double[] latValues(int docId);

	/**
	 * Lon values.
	 *
	 * @param docId the doc id
	 * @return the double[]
	 */
	abstract public double[] lonValues(int docId);

	/**
	 * Distance.
	 *
	 * @param docId the doc id
	 * @param unit the unit
	 * @param lat the lat
	 * @param lon the lon
	 * @return the double
	 */
	public double distance(int docId, DistanceUnit unit, double lat, double lon) {
		return GeoDistance.PLANE.calculate(latValue(docId), lonValue(docId), lat, lon, unit);
	}

	/**
	 * Arc distance.
	 *
	 * @param docId the doc id
	 * @param unit the unit
	 * @param lat the lat
	 * @param lon the lon
	 * @return the double
	 */
	public double arcDistance(int docId, DistanceUnit unit, double lat, double lon) {
		return GeoDistance.ARC.calculate(latValue(docId), lonValue(docId), lat, lon, unit);
	}

	/**
	 * Factor distance.
	 *
	 * @param docId the doc id
	 * @param unit the unit
	 * @param lat the lat
	 * @param lon the lon
	 * @return the double
	 */
	public double factorDistance(int docId, DistanceUnit unit, double lat, double lon) {
		return GeoDistance.FACTOR.calculate(latValue(docId), lonValue(docId), lat, lon, unit);
	}

	/**
	 * Distance geohash.
	 *
	 * @param docId the doc id
	 * @param unit the unit
	 * @param geoHash the geo hash
	 * @return the double
	 */
	public double distanceGeohash(int docId, DistanceUnit unit, String geoHash) {
		GeoPointHash geoPointHash = geoHashCache.get().get();
		if (geoPointHash.geoHash != geoHash) {
			geoPointHash.geoHash = geoHash;
			double[] decode = GeoHashUtils.decode(geoHash);
			geoPointHash.lat = decode[0];
			geoPointHash.lon = decode[1];
		}
		return GeoDistance.PLANE.calculate(latValue(docId), lonValue(docId), geoPointHash.lat, geoPointHash.lon, unit);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#docFieldData(int)
	 */
	@Override
	public GeoPointDocFieldData docFieldData(int docId) {
		return super.docFieldData(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#computeSizeInBytes()
	 */
	@Override
	protected long computeSizeInBytes() {
		return (RamUsage.NUM_BYTES_DOUBLE * lat.length + RamUsage.NUM_BYTES_ARRAY_HEADER)
				+ (RamUsage.NUM_BYTES_DOUBLE * lon.length + RamUsage.NUM_BYTES_ARRAY_HEADER);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#stringValue(int)
	 */
	@Override
	public String stringValue(int docId) {
		return value(docId).geohash();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#createFieldData()
	 */
	@Override
	protected GeoPointDocFieldData createFieldData() {
		return new GeoPointDocFieldData(this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#type()
	 */
	@Override
	public FieldDataType type() {
		return GeoPointFieldDataType.TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#forEachValue(cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc)
	 */
	@Override
	public void forEachValue(StringValueProc proc) {
		for (int i = 1; i < lat.length; i++) {
			proc.onValue(GeoHashUtils.encode(lat[i], lon[i]));
		}
	}

	/**
	 * For each value.
	 *
	 * @param proc the proc
	 */
	public void forEachValue(PointValueProc proc) {
		for (int i = 1; i < lat.length; i++) {
			GeoPoint point = valuesCache.get().get();
			point.latlon(lat[i], lon[i]);
			proc.onValue(point);
		}
	}

	/**
	 * The Interface PointValueProc.
	 *
	 * @author l.xue.nong
	 */
	public static interface PointValueProc {

		/**
		 * On value.
		 *
		 * @param value the value
		 */
		void onValue(GeoPoint value);
	}

	/**
	 * For each value.
	 *
	 * @param proc the proc
	 */
	public void forEachValue(ValueProc proc) {
		for (int i = 1; i < lat.length; i++) {
			proc.onValue(lat[i], lon[i]);
		}
	}

	/**
	 * The Interface ValueProc.
	 *
	 * @author l.xue.nong
	 */
	public static interface ValueProc {

		/**
		 * On value.
		 *
		 * @param lat the lat
		 * @param lon the lon
		 */
		void onValue(double lat, double lon);
	}

	/**
	 * For each value in doc.
	 *
	 * @param docId the doc id
	 * @param proc the proc
	 */
	public abstract void forEachValueInDoc(int docId, ValueInDocProc proc);

	/**
	 * The Interface ValueInDocProc.
	 *
	 * @author l.xue.nong
	 */
	public static interface ValueInDocProc {

		/**
		 * On value.
		 *
		 * @param docId the doc id
		 * @param lat the lat
		 * @param lon the lon
		 */
		void onValue(int docId, double lat, double lon);
	}

	/**
	 * Load.
	 *
	 * @param reader the reader
	 * @param field the field
	 * @return the geo point field data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static GeoPointFieldData load(IndexReader reader, String field) throws IOException {
		return FieldDataLoader.load(reader, field, new StringTypeLoader());
	}

	/**
	 * The Class StringTypeLoader.
	 *
	 * @author l.xue.nong
	 */
	static class StringTypeLoader extends FieldDataLoader.FreqsTypeLoader<GeoPointFieldData> {

		/** The lat. */
		private final TDoubleArrayList lat = new TDoubleArrayList();

		/** The lon. */
		private final TDoubleArrayList lon = new TDoubleArrayList();

		/**
		 * Instantiates a new string type loader.
		 */
		StringTypeLoader() {
			super();

			lat.add(0);
			lon.add(0);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#collectTerm(java.lang.String)
		 */
		@Override
		public void collectTerm(String term) {
			int comma = term.indexOf(',');
			lat.add(Double.parseDouble(term.substring(0, comma)));
			lon.add(Double.parseDouble(term.substring(comma + 1)));

		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildSingleValue(java.lang.String, int[])
		 */
		@Override
		public GeoPointFieldData buildSingleValue(String field, int[] ordinals) {
			return new SingleValueGeoPointFieldData(field, ordinals, lat.toArray(), lon.toArray());
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildMultiValue(java.lang.String, int[][])
		 */
		@Override
		public GeoPointFieldData buildMultiValue(String field, int[][] ordinals) {
			return new MultiValueGeoPointFieldData(field, ordinals, lat.toArray(), lon.toArray());
		}
	}
}
