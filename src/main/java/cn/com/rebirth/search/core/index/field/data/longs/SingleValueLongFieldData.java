/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SingleValueLongFieldData.java 2012-3-29 15:02:06 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.longs;

import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.doubles.DoubleFieldData;


/**
 * The Class SingleValueLongFieldData.
 *
 * @author l.xue.nong
 */
public class SingleValueLongFieldData extends LongFieldData {

	
	/** The doubles values cache. */
	private ThreadLocal<ThreadLocals.CleanableValue<double[]>> doublesValuesCache = new ThreadLocal<ThreadLocals.CleanableValue<double[]>>() {
		@Override
		protected ThreadLocals.CleanableValue<double[]> initialValue() {
			return new ThreadLocals.CleanableValue<double[]>(new double[1]);
		}
	};

	
	/** The dates values cache. */
	private ThreadLocal<ThreadLocals.CleanableValue<MutableDateTime[]>> datesValuesCache = new ThreadLocal<ThreadLocals.CleanableValue<MutableDateTime[]>>() {
		@Override
		protected ThreadLocals.CleanableValue<MutableDateTime[]> initialValue() {
			MutableDateTime[] date = new MutableDateTime[1];
			date[0] = new MutableDateTime(DateTimeZone.UTC);
			return new ThreadLocals.CleanableValue<MutableDateTime[]>(date);
		}
	};

	
	/** The values cache. */
	private ThreadLocal<long[]> valuesCache = new ThreadLocal<long[]>() {
		@Override
		protected long[] initialValue() {
			return new long[1];
		}
	};

	
	
	/** The ordinals. */
	private final int[] ordinals;

	
	/**
	 * Instantiates a new single value long field data.
	 *
	 * @param fieldName the field name
	 * @param ordinals the ordinals
	 * @param values the values
	 */
	public SingleValueLongFieldData(String fieldName, int[] ordinals, long[] values) {
		super(fieldName, values);
		this.ordinals = ordinals;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#computeSizeInBytes()
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
		proc.onValue(docId, Long.toString(values[loc]));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.NumericFieldData.DoubleValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, DoubleValueInDocProc proc) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return;
		}
		proc.onValue(docId, values[loc]);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.NumericFieldData.LongValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, LongValueInDocProc proc) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return;
		}
		proc.onValue(docId, values[loc]);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.NumericFieldData.MissingDoubleValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, MissingDoubleValueInDocProc proc) {
		int loc = ordinals[docId];
		if (loc == 0) {
			proc.onMissing(docId);
			return;
		}
		proc.onValue(docId, values[loc]);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.NumericFieldData.MissingLongValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, MissingLongValueInDocProc proc) {
		int loc = ordinals[docId];
		if (loc == 0) {
			proc.onMissing(docId);
			return;
		}
		proc.onValue(docId, values[loc]);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#forEachOrdinalInDoc(int, cn.com.summall.search.core.index.field.data.FieldData.OrdinalInDocProc)
	 */
	@Override
	public void forEachOrdinalInDoc(int docId, OrdinalInDocProc proc) {
		proc.onOrdinal(docId, ordinals[docId]);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.longs.LongFieldData.ValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, ValueInDocProc proc) {
		int loc = ordinals[docId];
		if (loc == 0) {
			proc.onMissing(docId);
			return;
		}
		proc.onValue(docId, values[loc]);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.longs.LongFieldData.DateValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, DateValueInDocProc proc) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return;
		}
		MutableDateTime dateTime = dateTimeCache.get().get();
		dateTime.setMillis(values[loc]);
		proc.onValue(docId, dateTime);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#forEachValueInDoc(int, org.joda.time.MutableDateTime, cn.com.summall.search.core.index.field.data.longs.LongFieldData.DateValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, MutableDateTime dateTime, DateValueInDocProc proc) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return;
		}
		dateTime.setMillis(values[loc]);
		proc.onValue(docId, dateTime);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#dates(int)
	 */
	@Override
	public MutableDateTime[] dates(int docId) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return EMPTY_DATETIME_ARRAY;
		}
		MutableDateTime[] ret = datesValuesCache.get().get();
		ret[0].setMillis(values[loc]);
		return ret;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.NumericFieldData#doubleValues(int)
	 */
	@Override
	public double[] doubleValues(int docId) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return DoubleFieldData.EMPTY_DOUBLE_ARRAY;
		}
		double[] ret = doublesValuesCache.get().get();
		ret[0] = values[loc];
		return ret;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#value(int)
	 */
	@Override
	public long value(int docId) {
		return values[ordinals[docId]];
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.longs.LongFieldData#values(int)
	 */
	@Override
	public long[] values(int docId) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return EMPTY_LONG_ARRAY;
		}
		long[] ret = valuesCache.get();
		ret[0] = values[loc];
		return ret;
	}
}