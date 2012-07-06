/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SingleValueGeoPointFieldData.java 2012-3-29 15:01:57 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.geo;

import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.doubles.DoubleFieldData;
import cn.com.rebirth.search.core.index.search.geo.GeoHashUtils;


/**
 * The Class SingleValueGeoPointFieldData.
 *
 * @author l.xue.nong
 */
public class SingleValueGeoPointFieldData extends GeoPointFieldData {

	
	/** The values array cache. */
	private static ThreadLocal<ThreadLocals.CleanableValue<GeoPoint[]>> valuesArrayCache = new ThreadLocal<ThreadLocals.CleanableValue<GeoPoint[]>>() {
		@Override
		protected ThreadLocals.CleanableValue<GeoPoint[]> initialValue() {
			GeoPoint[] value = new GeoPoint[1];
			value[0] = new GeoPoint();
			return new ThreadLocals.CleanableValue<GeoPoint[]>(value);
		}
	};

	
	/** The values lat cache. */
	private ThreadLocal<ThreadLocals.CleanableValue<double[]>> valuesLatCache = new ThreadLocal<ThreadLocals.CleanableValue<double[]>>() {
		@Override
		protected ThreadLocals.CleanableValue<double[]> initialValue() {
			return new ThreadLocals.CleanableValue<double[]>(new double[1]);
		}
	};

	
	/** The values lon cache. */
	private ThreadLocal<ThreadLocals.CleanableValue<double[]>> valuesLonCache = new ThreadLocal<ThreadLocals.CleanableValue<double[]>>() {
		@Override
		protected ThreadLocals.CleanableValue<double[]> initialValue() {
			return new ThreadLocals.CleanableValue<double[]>(new double[1]);
		}
	};

	
	
	/** The ordinals. */
	private final int[] ordinals;

	
	/**
	 * Instantiates a new single value geo point field data.
	 *
	 * @param fieldName the field name
	 * @param ordinals the ordinals
	 * @param lat the lat
	 * @param lon the lon
	 */
	public SingleValueGeoPointFieldData(String fieldName, int[] ordinals, double[] lat, double[] lon) {
		super(fieldName, lat, lon);
		this.ordinals = ordinals;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#computeSizeInBytes()
	 */
	@Override
	protected long computeSizeInBytes() {
		return super.computeSizeInBytes() + RamUsage.NUM_BYTES_INT * ordinals.length + RamUsage.NUM_BYTES_ARRAY_HEADER;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#multiValued()
	 */
	@Override
	public boolean multiValued() {
		return false;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#hasValue(int)
	 */
	@Override
	public boolean hasValue(int docId) {
		return ordinals[docId] != 0;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.FieldData.StringValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, StringValueInDocProc proc) {
		int loc = ordinals[docId];
		if (loc == 0) {
			proc.onMissing(docId);
			return;
		}
		proc.onValue(docId, GeoHashUtils.encode(lat[loc], lon[loc]));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#forEachOrdinalInDoc(int, cn.com.summall.search.core.index.field.data.FieldData.OrdinalInDocProc)
	 */
	@Override
	public void forEachOrdinalInDoc(int docId, OrdinalInDocProc proc) {
		proc.onOrdinal(docId, ordinals[docId]);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData.ValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, ValueInDocProc proc) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return;
		}
		proc.onValue(docId, lat[loc], lon[loc]);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#value(int)
	 */
	@Override
	public GeoPoint value(int docId) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return null;
		}
		GeoPoint point = valuesCache.get().get();
		point.latlon(lat[loc], lon[loc]);
		return point;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#values(int)
	 */
	@Override
	public GeoPoint[] values(int docId) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return EMPTY_ARRAY;
		}
		GeoPoint[] ret = valuesArrayCache.get().get();
		ret[0].latlon(lat[loc], lon[loc]);
		return ret;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#latValue(int)
	 */
	@Override
	public double latValue(int docId) {
		return lat[ordinals[docId]];
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#lonValue(int)
	 */
	@Override
	public double lonValue(int docId) {
		return lon[ordinals[docId]];
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#latValues(int)
	 */
	@Override
	public double[] latValues(int docId) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return DoubleFieldData.EMPTY_DOUBLE_ARRAY;
		}
		double[] ret = valuesLatCache.get().get();
		ret[0] = lat[loc];
		return ret;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#lonValues(int)
	 */
	@Override
	public double[] lonValues(int docId) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return DoubleFieldData.EMPTY_DOUBLE_ARRAY;
		}
		double[] ret = valuesLonCache.get().get();
		ret[0] = lon[loc];
		return ret;
	}
}
