/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MultiValueLongFieldData.java 2012-3-29 15:02:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.longs;

import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.doubles.DoubleFieldData;


/**
 * The Class MultiValueLongFieldData.
 *
 * @author l.xue.nong
 */
public class MultiValueLongFieldData extends LongFieldData {

	
	/** The Constant VALUE_CACHE_SIZE. */
	private static final int VALUE_CACHE_SIZE = 10;

	
	/** The doubles values cache. */
	private ThreadLocal<ThreadLocals.CleanableValue<double[][]>> doublesValuesCache = new ThreadLocal<ThreadLocals.CleanableValue<double[][]>>() {
		@Override
		protected ThreadLocals.CleanableValue<double[][]> initialValue() {
			double[][] value = new double[VALUE_CACHE_SIZE][];
			for (int i = 0; i < value.length; i++) {
				value[i] = new double[i];
			}
			return new ThreadLocals.CleanableValue<double[][]>(value);
		}
	};

	
	/** The date times cache. */
	private ThreadLocal<ThreadLocals.CleanableValue<MutableDateTime[][]>> dateTimesCache = new ThreadLocal<ThreadLocals.CleanableValue<MutableDateTime[][]>>() {
		@Override
		protected ThreadLocals.CleanableValue<MutableDateTime[][]> initialValue() {
			MutableDateTime[][] value = new MutableDateTime[VALUE_CACHE_SIZE][];
			for (int i = 0; i < value.length; i++) {
				value[i] = new MutableDateTime[i];
				for (int j = 0; j < i; j++) {
					value[i][j] = new MutableDateTime(DateTimeZone.UTC);
				}
			}
			return new ThreadLocals.CleanableValue<MutableDateTime[][]>(value);
		}
	};

	
	/** The values cache. */
	private ThreadLocal<ThreadLocals.CleanableValue<long[][]>> valuesCache = new ThreadLocal<ThreadLocals.CleanableValue<long[][]>>() {
		@Override
		protected ThreadLocals.CleanableValue<long[][]> initialValue() {
			long[][] value = new long[VALUE_CACHE_SIZE][];
			for (int i = 0; i < value.length; i++) {
				value[i] = new long[i];
			}
			return new ThreadLocals.CleanableValue<long[][]>(value);
		}
	};

	
	
	/** The ordinals. */
	private final int[][] ordinals;

	
	/**
	 * Instantiates a new multi value long field data.
	 *
	 * @param fieldName the field name
	 * @param ordinals the ordinals
	 * @param values the values
	 */
	public MultiValueLongFieldData(String fieldName, int[][] ordinals, long[] values) {
		super(fieldName, values);
		this.ordinals = ordinals;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#computeSizeInBytes()
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
				proc.onValue(docId, Long.toString(values[loc]));
			}
		}
		if (!found) {
			proc.onMissing(docId);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.NumericFieldData.DoubleValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, DoubleValueInDocProc proc) {
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				proc.onValue(docId, values[loc]);
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.NumericFieldData.LongValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, LongValueInDocProc proc) {
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				proc.onValue(docId, values[loc]);
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.NumericFieldData.MissingDoubleValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, MissingDoubleValueInDocProc proc) {
		boolean found = false;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				found = true;
				proc.onValue(docId, values[loc]);
			}
		}
		if (!found) {
			proc.onMissing(docId);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.NumericFieldData.MissingLongValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, MissingLongValueInDocProc proc) {
		boolean found = false;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				found = true;
				proc.onValue(docId, values[loc]);
			}
		}
		if (!found) {
			proc.onMissing(docId);
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
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.longs.LongFieldData.ValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, ValueInDocProc proc) {
		boolean found = false;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				found = true;
				proc.onValue(docId, values[loc]);
			}
		}
		if (!found) {
			proc.onMissing(docId);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.longs.LongFieldData.DateValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, DateValueInDocProc proc) {
		MutableDateTime dateTime = dateTimeCache.get().get();
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				dateTime.setMillis(values[loc]);
				proc.onValue(docId, dateTime);
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#forEachValueInDoc(int, org.joda.time.MutableDateTime, cn.com.summall.search.core.index.field.data.longs.LongFieldData.DateValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, MutableDateTime dateTime, DateValueInDocProc proc) {
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				dateTime.setMillis(values[loc]);
				proc.onValue(docId, dateTime);
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#dates(int)
	 */
	@Override
	public MutableDateTime[] dates(int docId) {
		int length = 0;
		for (int[] ordinal : ordinals) {
			if (ordinal[docId] != 0) {
				length++;
			}
		}
		if (length == 0) {
			return EMPTY_DATETIME_ARRAY;
		}
		MutableDateTime[] dates;
		if (length < VALUE_CACHE_SIZE) {
			dates = dateTimesCache.get().get()[length];
		} else {
			dates = new MutableDateTime[length];
			for (int i = 0; i < dates.length; i++) {
				dates[i] = new MutableDateTime();
			}
		}
		int i = 0;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				dates[i++].setMillis(values[loc]);
			}
		}
		return dates;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.NumericFieldData#doubleValues(int)
	 */
	@Override
	public double[] doubleValues(int docId) {
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
			doubles = doublesValuesCache.get().get()[length];
		} else {
			doubles = new double[length];
		}
		int i = 0;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				doubles[i++] = values[loc];
			}
		}
		return doubles;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#value(int)
	 */
	@Override
	public long value(int docId) {
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				return values[loc];
			}
		}
		return 0;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#values(int)
	 */
	@Override
	public long[] values(int docId) {
		int length = 0;
		for (int[] ordinal : ordinals) {
			if (ordinal[docId] != 0) {
				length++;
			}
		}
		if (length == 0) {
			return EMPTY_LONG_ARRAY;
		}
		long[] longs;
		if (length < VALUE_CACHE_SIZE) {
			longs = valuesCache.get().get()[length];
		} else {
			longs = new long[length];
		}
		int i = 0;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				longs[i++] = values[loc];
			}
		}
		return longs;
	}
}