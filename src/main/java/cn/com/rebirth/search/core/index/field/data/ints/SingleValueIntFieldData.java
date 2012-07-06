/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SingleValueIntFieldData.java 2012-7-6 14:29:13 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.ints;

import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.doubles.DoubleFieldData;

/**
 * The Class SingleValueIntFieldData.
 *
 * @author l.xue.nong
 */
public class SingleValueIntFieldData extends IntFieldData {

	/** The doubles values cache. */
	private ThreadLocal<ThreadLocals.CleanableValue<double[]>> doublesValuesCache = new ThreadLocal<ThreadLocals.CleanableValue<double[]>>() {
		@Override
		protected ThreadLocals.CleanableValue<double[]> initialValue() {
			return new ThreadLocals.CleanableValue<double[]>(new double[1]);
		}
	};

	/** The values cache. */
	private ThreadLocal<ThreadLocals.CleanableValue<int[]>> valuesCache = new ThreadLocal<ThreadLocals.CleanableValue<int[]>>() {
		@Override
		protected ThreadLocals.CleanableValue<int[]> initialValue() {
			return new ThreadLocals.CleanableValue<int[]>(new int[1]);
		}
	};

	/** The ordinals. */
	private final int[] ordinals;

	/**
	 * Instantiates a new single value int field data.
	 *
	 * @param fieldName the field name
	 * @param ordinals the ordinals
	 * @param values the values
	 */
	public SingleValueIntFieldData(String fieldName, int[] ordinals, int[] values) {
		super(fieldName, values);
		this.ordinals = ordinals;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.ints.IntFieldData#computeSizeInBytes()
	 */
	@Override
	protected long computeSizeInBytes() {
		return super.computeSizeInBytes() + RamUsage.NUM_BYTES_INT * ordinals.length + RamUsage.NUM_BYTES_ARRAY_HEADER;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#multiValued()
	 */
	@Override
	public boolean multiValued() {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#hasValue(int)
	 */
	@Override
	public boolean hasValue(int docId) {
		return ordinals[docId] != 0;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#forEachValueInDoc(int, cn.com.rebirth.search.core.index.field.data.FieldData.StringValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, StringValueInDocProc proc) {
		int loc = ordinals[docId];
		if (loc == 0) {
			proc.onMissing(docId);
			return;
		}
		proc.onValue(docId, Integer.toString(values[loc]));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.rebirth.search.core.index.field.data.NumericFieldData.DoubleValueInDocProc)
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
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.rebirth.search.core.index.field.data.NumericFieldData.LongValueInDocProc)
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
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.rebirth.search.core.index.field.data.NumericFieldData.MissingDoubleValueInDocProc)
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
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.rebirth.search.core.index.field.data.NumericFieldData.MissingLongValueInDocProc)
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
	 * @see cn.com.rebirth.search.core.index.field.data.ints.IntFieldData#forEachValueInDoc(int, cn.com.rebirth.search.core.index.field.data.ints.IntFieldData.ValueInDocProc)
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
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#forEachOrdinalInDoc(int, cn.com.rebirth.search.core.index.field.data.FieldData.OrdinalInDocProc)
	 */
	@Override
	public void forEachOrdinalInDoc(int docId, OrdinalInDocProc proc) {
		proc.onOrdinal(docId, ordinals[docId]);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#doubleValues(int)
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
	 * @see cn.com.rebirth.search.core.index.field.data.ints.IntFieldData#value(int)
	 */
	@Override
	public int value(int docId) {
		return values[ordinals[docId]];
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.ints.IntFieldData#values(int)
	 */
	@Override
	public int[] values(int docId) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return EMPTY_INT_ARRAY;
		}
		int[] ret = valuesCache.get().get();
		ret[0] = values[loc];
		return ret;
	}
}