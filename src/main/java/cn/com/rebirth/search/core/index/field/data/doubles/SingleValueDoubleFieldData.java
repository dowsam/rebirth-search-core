/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SingleValueDoubleFieldData.java 2012-3-29 15:02:52 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.doubles;

import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.RamUsage;


/**
 * The Class SingleValueDoubleFieldData.
 *
 * @author l.xue.nong
 */
public class SingleValueDoubleFieldData extends DoubleFieldData {

	
	/** The values cache. */
	private ThreadLocal<ThreadLocals.CleanableValue<double[]>> valuesCache = new ThreadLocal<ThreadLocals.CleanableValue<double[]>>() {
		@Override
		protected ThreadLocals.CleanableValue<double[]> initialValue() {
			return new ThreadLocals.CleanableValue<double[]>(new double[1]);
		}
	};

	
	
	/** The ordinals. */
	private final int[] ordinals;

	
	/**
	 * Instantiates a new single value double field data.
	 *
	 * @param fieldName the field name
	 * @param ordinals the ordinals
	 * @param values the values
	 */
	public SingleValueDoubleFieldData(String fieldName, int[] ordinals, double[] values) {
		super(fieldName, values);
		this.ordinals = ordinals;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.doubles.DoubleFieldData#computeSizeInBytes()
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
		proc.onValue(docId, Double.toString(values[loc]));
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
		proc.onValue(docId, (long) values[loc]);
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
		proc.onValue(docId, (long) values[loc]);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.doubles.DoubleFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.doubles.DoubleFieldData.ValueInDocProc)
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
	 * @see cn.com.summall.search.core.index.field.data.FieldData#forEachOrdinalInDoc(int, cn.com.summall.search.core.index.field.data.FieldData.OrdinalInDocProc)
	 */
	@Override
	public void forEachOrdinalInDoc(int docId, OrdinalInDocProc proc) {
		proc.onOrdinal(docId, ordinals[docId]);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.NumericFieldData#doubleValues(int)
	 */
	@Override
	public double[] doubleValues(int docId) {
		return values(docId);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.doubles.DoubleFieldData#value(int)
	 */
	@Override
	public double value(int docId) {
		return values[ordinals[docId]];
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.doubles.DoubleFieldData#values(int)
	 */
	@Override
	public double[] values(int docId) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return EMPTY_DOUBLE_ARRAY;
		}
		double[] ret = valuesCache.get().get();
		ret[0] = values[loc];
		return ret;
	}
}