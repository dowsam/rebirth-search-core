/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MultiValueShortFieldData.java 2012-3-29 15:02:47 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.shorts;

import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.doubles.DoubleFieldData;


/**
 * The Class MultiValueShortFieldData.
 *
 * @author l.xue.nong
 */
public class MultiValueShortFieldData extends ShortFieldData {

	
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

	
	/** The values cache. */
	private ThreadLocal<ThreadLocals.CleanableValue<short[][]>> valuesCache = new ThreadLocal<ThreadLocals.CleanableValue<short[][]>>() {
		@Override
		protected ThreadLocals.CleanableValue<short[][]> initialValue() {
			short[][] value = new short[VALUE_CACHE_SIZE][];
			for (int i = 0; i < value.length; i++) {
				value[i] = new short[i];
			}
			return new ThreadLocals.CleanableValue<short[][]>(value);
		}
	};

	
	
	/** The ordinals. */
	private final int[][] ordinals;

	
	/**
	 * Instantiates a new multi value short field data.
	 *
	 * @param fieldName the field name
	 * @param ordinals the ordinals
	 * @param values the values
	 */
	public MultiValueShortFieldData(String fieldName, int[][] ordinals, short[] values) {
		super(fieldName, values);
		this.ordinals = ordinals;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.shorts.ShortFieldData#computeSizeInBytes()
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
				proc.onValue(docId, Short.toString(values[loc]));
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
	 * @see cn.com.summall.search.core.index.field.data.shorts.ShortFieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.shorts.ShortFieldData.ValueInDocProc)
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
	 * @see cn.com.summall.search.core.index.field.data.shorts.ShortFieldData#value(int)
	 */
	@Override
	public short value(int docId) {
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				return values[loc];
			}
		}
		return 0;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.shorts.ShortFieldData#values(int)
	 */
	@Override
	public short[] values(int docId) {
		int length = 0;
		for (int[] ordinal : ordinals) {
			if (ordinal[docId] != 0) {
				length++;
			}
		}
		if (length == 0) {
			return EMPTY_SHORT_ARRAY;
		}
		short[] shorts;
		if (length < VALUE_CACHE_SIZE) {
			shorts = valuesCache.get().get()[length];
		} else {
			shorts = new short[length];
		}
		int i = 0;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				shorts[i++] = values[loc];
			}
		}
		return shorts;
	}
}