/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MultiValueGeoPointFieldData.java 2012-3-29 15:02:36 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.geo;

import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.doubles.DoubleFieldData;
import cn.com.rebirth.search.core.index.search.geo.GeoHashUtils;


/**
 * The Class MultiValueGeoPointFieldData.
 *
 * @author l.xue.nong
 */
public class MultiValueGeoPointFieldData extends GeoPointFieldData {

	
	/** The Constant VALUE_CACHE_SIZE. */
	private static final int VALUE_CACHE_SIZE = 100;

	
	/** The values array cache. */
	private static ThreadLocal<ThreadLocals.CleanableValue<GeoPoint[][]>> valuesArrayCache = new ThreadLocal<ThreadLocals.CleanableValue<GeoPoint[][]>>() {
		@Override
		protected ThreadLocals.CleanableValue<GeoPoint[][]> initialValue() {
			GeoPoint[][] value = new GeoPoint[VALUE_CACHE_SIZE][];
			for (int i = 0; i < value.length; i++) {
				value[i] = new GeoPoint[i];
				for (int j = 0; j < value[i].length; j++) {
					value[i][j] = new GeoPoint();
				}
			}
			return new ThreadLocals.CleanableValue<GeoPoint[][]>(value);
		}
	};

	
	/** The values lat cache. */
	private ThreadLocal<ThreadLocals.CleanableValue<double[][]>> valuesLatCache = new ThreadLocal<ThreadLocals.CleanableValue<double[][]>>() {
		@Override
		protected ThreadLocals.CleanableValue<double[][]> initialValue() {
			double[][] value = new double[VALUE_CACHE_SIZE][];
			for (int i = 0; i < value.length; i++) {
				value[i] = new double[i];
			}
			return new ThreadLocals.CleanableValue<double[][]>(value);
		}
	};

	
	/** The values lon cache. */
	private ThreadLocal<ThreadLocals.CleanableValue<double[][]>> valuesLonCache = new ThreadLocal<ThreadLocals.CleanableValue<double[][]>>() {
		@Override
		protected ThreadLocals.CleanableValue<double[][]> initialValue() {
			double[][] value = new double[VALUE_CACHE_SIZE][];
			for (int i = 0; i < value.length; i++) {
				value[i] = new double[i];
			}
			return new ThreadLocals.CleanableValue<double[][]>(value);
		}
	};

	
	
	/** The ordinals. */
	private final int[][] ordinals;

	
	/**
	 * Instantiates a new multi value geo point field data.
	 *
	 * @param fieldName the field name
	 * @param ordinals the ordinals
	 * @param lat the lat
	 * @param lon the lon
	 */
	public MultiValueGeoPointFieldData(String fieldName, int[][] ordinals, double[] lat, double[] lon) {
		super(fieldName, lat, lon);
		this.ordinals = ordinals;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#computeSizeInBytes()
	 */
	@Override
	protected long computeSizeInBytes() {
		long size = super.computeSizeInBytes();
		size += RamUsage.NUM_BYTES_ARRAY_HEADER; 
		for (int[] ordinal : ordinals) {
			size += RamUsage.NUM_BYTES_INT * ordinal.length + RamUsage.NUM_BYTES_ARRAY_HEADER;
		}
		return size;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#multiValued()
	 */
	@Override
	public boolean multiValued() {
		return true;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#hasValue(int)
	 */
	@Override
	public boolean hasValue(int docId) {
		for (int[] ordinal : ordinals) {
			if (ordinal[docId] != 0) {
				return true;
			}
		}
		return false;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.FieldData.StringValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, StringValueInDocProc proc) {
		boolean found = false;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				found = true;
				proc.onValue(docId, GeoHashUtils.encode(lat[loc], lon[loc]));
			}
		}
		if (!found) {
			proc.onMissing(docId);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData.ValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, ValueInDocProc proc) {
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				proc.onValue(docId, lat[loc], lon[loc]);
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#forEachOrdinalInDoc(int, cn.com.summall.search.core.index.field.data.FieldData.OrdinalInDocProc)
	 */
	@Override
	public void forEachOrdinalInDoc(int docId, OrdinalInDocProc proc) {
		boolean found = false;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				found = true;
				proc.onOrdinal(docId, loc);
			}
		}
		if (!found) {
			proc.onOrdinal(docId, 0);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#value(int)
	 */
	@Override
	public GeoPoint value(int docId) {
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				GeoPoint point = valuesCache.get().get();
				point.latlon(lat[loc], lon[loc]);
				return point;
			}
		}
		return null;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#values(int)
	 */
	@Override
	public GeoPoint[] values(int docId) {
		int length = 0;
		for (int[] ordinal : ordinals) {
			if (ordinal[docId] != 0) {
				length++;
			}
		}
		if (length == 0) {
			return EMPTY_ARRAY;
		}
		GeoPoint[] points;
		if (length < VALUE_CACHE_SIZE) {
			points = valuesArrayCache.get().get()[length];
			int i = 0;
			for (int[] ordinal : ordinals) {
				int loc = ordinal[docId];
				if (loc != 0) {
					points[i++].latlon(lat[loc], lon[loc]);
				}
			}
		} else {
			points = new GeoPoint[length];
			int i = 0;
			for (int[] ordinal : ordinals) {
				int loc = ordinal[docId];
				if (loc != 0) {
					points[i++] = new GeoPoint(lat[loc], lon[loc]);
				}
			}
		}
		return points;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#latValue(int)
	 */
	@Override
	public double latValue(int docId) {
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				return lat[loc];
			}
		}
		return 0;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#lonValue(int)
	 */
	@Override
	public double lonValue(int docId) {
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				return lon[loc];
			}
		}
		return 0;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#latValues(int)
	 */
	@Override
	public double[] latValues(int docId) {
		int length = 0;
		for (int[] ordinal : ordinals) {
			if (ordinal[docId] != 0) {
				length++;
			}
		}
		if (length == 0) {
			return DoubleFieldData.EMPTY_DOUBLE_ARRAY;
		}
		double[] doubles;
		if (length < VALUE_CACHE_SIZE) {
			doubles = valuesLatCache.get().get()[length];
		} else {
			doubles = new double[length];
		}
		int i = 0;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				doubles[i++] = lat[loc];
			}
		}
		return doubles;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData#lonValues(int)
	 */
	@Override
	public double[] lonValues(int docId) {
		int length = 0;
		for (int[] ordinal : ordinals) {
			if (ordinal[docId] != 0) {
				length++;
			}
		}
		if (length == 0) {
			return DoubleFieldData.EMPTY_DOUBLE_ARRAY;
		}
		double[] doubles;
		if (length < VALUE_CACHE_SIZE) {
			doubles = valuesLonCache.get().get()[length];
		} else {
			doubles = new double[length];
		}
		int i = 0;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				doubles[i++] = lon[loc];
			}
		}
		return doubles;
	}
}