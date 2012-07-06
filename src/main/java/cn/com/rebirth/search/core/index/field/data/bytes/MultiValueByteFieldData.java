/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MultiValueByteFieldData.java 2012-7-6 14:29:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.bytes;

import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.doubles.DoubleFieldData;

/**
 * The Class MultiValueByteFieldData.
 *
 * @author l.xue.nong
 */
public class MultiValueByteFieldData extends ByteFieldData {

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
	private ThreadLocal<ThreadLocals.CleanableValue<byte[][]>> valuesCache = new ThreadLocal<ThreadLocals.CleanableValue<byte[][]>>() {
		@Override
		protected ThreadLocals.CleanableValue<byte[][]> initialValue() {
			byte[][] value = new byte[VALUE_CACHE_SIZE][];
			for (int i = 0; i < value.length; i++) {
				value[i] = new byte[i];
			}
			return new ThreadLocals.CleanableValue<byte[][]>(value);
		}
	};

	/** The ordinals. */
	private final int[][] ordinals;

	/**
	 * Instantiates a new multi value byte field data.
	 *
	 * @param fieldName the field name
	 * @param ordinals the ordinals
	 * @param values the values
	 */
	public MultiValueByteFieldData(String fieldName, int[][] ordinals, byte[] values) {
		super(fieldName, values);
		this.ordinals = ordinals;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.bytes.ByteFieldData#computeSizeInBytes()
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
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#multiValued()
	 */
	@Override
	public boolean multiValued() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#hasValue(int)
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
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#forEachValueInDoc(int, cn.com.rebirth.search.core.index.field.data.FieldData.StringValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, StringValueInDocProc proc) {
		boolean found = false;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				found = true;
				proc.onValue(docId, Byte.toString(values[loc]));
			}
		}
		if (!found) {
			proc.onMissing(docId);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.rebirth.search.core.index.field.data.NumericFieldData.DoubleValueInDocProc)
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
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.rebirth.search.core.index.field.data.NumericFieldData.LongValueInDocProc)
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
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.rebirth.search.core.index.field.data.NumericFieldData.MissingDoubleValueInDocProc)
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
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#forEachValueInDoc(int, cn.com.rebirth.search.core.index.field.data.NumericFieldData.MissingLongValueInDocProc)
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
	 * @see cn.com.rebirth.search.core.index.field.data.bytes.ByteFieldData#forEachValueInDoc(int, cn.com.rebirth.search.core.index.field.data.bytes.ByteFieldData.ValueInDocProc)
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
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#forEachOrdinalInDoc(int, cn.com.rebirth.search.core.index.field.data.FieldData.OrdinalInDocProc)
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
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#doubleValues(int)
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
	 * @see cn.com.rebirth.search.core.index.field.data.bytes.ByteFieldData#value(int)
	 */
	@Override
	public byte value(int docId) {
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				return values[loc];
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.bytes.ByteFieldData#values(int)
	 */
	@Override
	public byte[] values(int docId) {
		int length = 0;
		for (int[] ordinal : ordinals) {
			if (ordinal[docId] != 0) {
				length++;
			}
		}
		if (length == 0) {
			return EMPTY_BYTE_ARRAY;
		}
		byte[] bytes;
		if (length < VALUE_CACHE_SIZE) {
			bytes = valuesCache.get().get()[length];
		} else {
			bytes = new byte[length];
		}
		int i = 0;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				bytes[i++] = values[loc];
			}
		}
		return bytes;
	}
}